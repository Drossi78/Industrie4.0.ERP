package com.stratiq.backend.shared.migration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Runs once at startup (profile "default") to apply the extend_schema migration
 * when psql is not available. Uses IF NOT EXISTS so it is safe to run repeatedly.
 */
@Component
@Profile("default")
@Order(0)
@Slf4j
@RequiredArgsConstructor
public class SchemaRunner implements ApplicationRunner {

    private final JdbcTemplate jdbc;

    @Override
    public void run(ApplicationArguments args) {
        try {
            log.info("SchemaRunner: applying extend_schema migration...");
            applyMigration();
            log.info("SchemaRunner: migration complete");
        } catch (Exception e) {
            log.warn("SchemaRunner failed (will continue): {}", e.getMessage());
        }
    }

    private void applyMigration() {
        // Item kind enum: idempotent, ignored if already exists.
        jdbc.execute("""
            DO $$ BEGIN
              CREATE TYPE public.item_kind_enum AS ENUM ('OWN_PRODUCT', 'PURCHASED');
            EXCEPTION WHEN duplicate_object THEN NULL; END $$;
            """);

        // Items table: soft-delete via is_deleted flag.
        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS public.items (
                id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                kind public.item_kind_enum NOT NULL DEFAULT 'OWN_PRODUCT',
                category_id UUID REFERENCES public.category(id),
                unit VARCHAR(20),
                price NUMERIC(12,2) DEFAULT 0,
                description TEXT,
                notes TEXT,
                is_deleted BOOLEAN NOT NULL DEFAULT false,
                created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
                updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
            )
            """);

        // Batches: metadata-only stock lots (number, expiry). Current stock is derived
        // from the stock movements referencing the batch, never stored here.
        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS public.batches (
                id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                batch_number VARCHAR(100) NOT NULL,
                item_id UUID NOT NULL REFERENCES public.items(id),
                warehouse_id UUID NOT NULL REFERENCES public.lager(id),
                expiry_date TIMESTAMP WITHOUT TIME ZONE,
                created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
                updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
            )
            """);
        // Drop the legacy denormalised stock column on databases created before this change.
        jdbc.execute("ALTER TABLE public.batches DROP COLUMN IF EXISTS current_stock");

        // BOM: tracks component quantities for manufactured assets. A component may not itself
        // be a BOM parent (enforced in BomServiceImpl) — no nested/sub-assembly BOMs.
        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS public.bills_of_material (
                id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                item_id UUID NOT NULL REFERENCES public.items(id),
                component_id UUID NOT NULL REFERENCES public.items(id),
                quantity NUMERIC(12,3) NOT NULL,
                unit VARCHAR(20),
                notes TEXT,
                created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
                updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
            )
            """);

        // Roles: user access control groups.
        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS public.roles (
                id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                name VARCHAR(100) NOT NULL UNIQUE,
                description TEXT,
                created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
                updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
            )
            """);

        // Permissions: granular access control flags.
        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS public.permissions (
                id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                codename VARCHAR(100) NOT NULL UNIQUE,
                created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
            )
            """);

        // role_permissions
        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS public.role_permissions (
                role_id UUID NOT NULL REFERENCES public.roles(id) ON DELETE CASCADE,
                permission_id UUID NOT NULL REFERENCES public.permissions(id) ON DELETE CASCADE,
                PRIMARY KEY (role_id, permission_id)
            )
            """);

        // Users: soft-delete via deleted_at timestamp.
        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS public.users (
                id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                email VARCHAR(255) NOT NULL UNIQUE,
                password VARCHAR(255) NOT NULL,
                role_id UUID REFERENCES public.roles(id),
                preferred_lang VARCHAR(10) NOT NULL DEFAULT 'de',
                is_active BOOLEAN NOT NULL DEFAULT true,
                created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
                updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
                deleted_at TIMESTAMP WITHOUT TIME ZONE
            )
            """);

        // Audit logs: change tracking for compliance and debugging.
        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS public.audit_logs (
                id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                user_id UUID,
                entity_type VARCHAR(100) NOT NULL,
                entity_id UUID,
                action VARCHAR(50) NOT NULL,
                old_value TEXT,
                new_value TEXT,
                created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
            )
            """);

        // Stock reservations: block available stock without changing physical balance.
        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS public.stock_reservation (
                id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                item_id UUID NOT NULL REFERENCES public.items(id),
                warehouse_id UUID NOT NULL REFERENCES public.lager(id),
                order_id UUID REFERENCES public.auftrag(id),
                quantity BIGINT NOT NULL,
                status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                note VARCHAR(255),
                released_at TIMESTAMP WITHOUT TIME ZONE,
                created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
            )
            """);

        // Batch tracking on stock movements: links each IN/OUT to a specific inventory lot.
        // Nullable so pre-batch data and untracked items keep working.
        jdbc.execute("ALTER TABLE public.stock_movement ADD COLUMN IF NOT EXISTS batch_id UUID REFERENCES public.batches(id)");
        jdbc.execute("CREATE INDEX IF NOT EXISTS idx_stock_movement_batch_id ON public.stock_movement(batch_id)");

        // Legacy rename: asset-named identifiers -> item (see migration
        // 20260712150000_rename_asset_names_to_item.sql). Guarded, so it is a
        // no-op on databases that are already renamed or freshly created.
        jdbc.execute("""
            DO $$ BEGIN
              IF EXISTS (SELECT 1 FROM information_schema.tables
                         WHERE table_schema = 'public' AND table_name = 'auftrag_assets') THEN
                ALTER TABLE public.auftrag_assets RENAME TO auftrag_items;
              END IF;
              IF EXISTS (SELECT 1 FROM information_schema.columns
                         WHERE table_schema = 'public' AND table_name = 'auftrag_items'
                           AND column_name = 'asset_id') THEN
                ALTER TABLE public.auftrag_items RENAME COLUMN asset_id TO item_id;
              END IF;
              IF EXISTS (SELECT 1 FROM information_schema.columns
                         WHERE table_schema = 'public' AND table_name = 'stock_movement'
                           AND column_name = 'asset_id') THEN
                ALTER TABLE public.stock_movement RENAME COLUMN asset_id TO item_id;
              END IF;
              IF EXISTS (SELECT 1 FROM information_schema.columns
                         WHERE table_schema = 'public' AND table_name = 'stock_reservation'
                           AND column_name = 'asset_id') THEN
                ALTER TABLE public.stock_reservation RENAME COLUMN asset_id TO item_id;
              END IF;
              IF EXISTS (SELECT 1 FROM information_schema.columns
                         WHERE table_schema = 'public' AND table_name = 'production_steps'
                           AND column_name = 'asset_id') THEN
                ALTER TABLE public.production_steps RENAME COLUMN asset_id TO item_id;
              END IF;
              IF EXISTS (SELECT 1 FROM information_schema.columns
                         WHERE table_schema = 'public' AND table_name = 'order_production_steps'
                           AND column_name = 'auftrag_asset_id') THEN
                ALTER TABLE public.order_production_steps RENAME COLUMN auftrag_asset_id TO auftrag_item_id;
              END IF;
            END $$;
            """);
    }
}

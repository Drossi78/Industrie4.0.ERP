-- Pure rename migration: the "asset -> item" refactor left legacy identifiers
-- behind (table auftrag_assets, columns *.asset_id / auftrag_asset_id). This
-- migration renames them to their item-based names. No semantic changes, all
-- data is preserved. Idempotent: every rename is guarded, safe to run twice.

-- 1. Order items table: auftrag_assets -> auftrag_items
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables
               WHERE table_schema = 'public' AND table_name = 'auftrag_assets') THEN
        ALTER TABLE public.auftrag_assets RENAME TO auftrag_items;
    END IF;
END $$;

-- 2. FK columns asset_id -> item_id (all point to items(id) since 20260710140000)
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = 'public' AND table_name = 'auftrag_items'
                 AND column_name = 'asset_id') THEN
        ALTER TABLE public.auftrag_items RENAME COLUMN asset_id TO item_id;
    END IF;
END $$;

DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = 'public' AND table_name = 'stock_movement'
                 AND column_name = 'asset_id') THEN
        ALTER TABLE public.stock_movement RENAME COLUMN asset_id TO item_id;
    END IF;
END $$;

DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = 'public' AND table_name = 'stock_reservation'
                 AND column_name = 'asset_id') THEN
        ALTER TABLE public.stock_reservation RENAME COLUMN asset_id TO item_id;
    END IF;
END $$;

DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = 'public' AND table_name = 'production_steps'
                 AND column_name = 'asset_id') THEN
        ALTER TABLE public.production_steps RENAME COLUMN asset_id TO item_id;
    END IF;
END $$;

-- 3. order_production_steps.auftrag_asset_id -> auftrag_item_id
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = 'public' AND table_name = 'order_production_steps'
                 AND column_name = 'auftrag_asset_id') THEN
        ALTER TABLE public.order_production_steps RENAME COLUMN auftrag_asset_id TO auftrag_item_id;
    END IF;
END $$;

-- 4. Rename the FK constraints that still carry "asset" in their name (cosmetic)
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_auftrag_assets_item') THEN
        ALTER TABLE public.auftrag_items RENAME CONSTRAINT fk_auftrag_assets_item TO fk_auftrag_items_item;
    END IF;
END $$;

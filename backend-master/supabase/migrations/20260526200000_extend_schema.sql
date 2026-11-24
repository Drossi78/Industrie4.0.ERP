-- Stratiq extended schema — Phase 2
-- Adds: item_kind_enum, items, batches, bills_of_material,
--        roles, permissions, role_permissions, users, audit_logs

-- ─── Enums ───────────────────────────────────────────────────────────────────
DO $$ BEGIN
  CREATE TYPE public.item_kind_enum AS ENUM ('OWN_PRODUCT', 'PURCHASED');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- ─── Items (product / article catalogue) ─────────────────────────────────────
CREATE TABLE IF NOT EXISTS public.items (
    id              UUID                     NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    name            VARCHAR(255)             NOT NULL,
    kind            public.item_kind_enum    NOT NULL DEFAULT 'OWN_PRODUCT',
    category_id     UUID                     REFERENCES public.category(id),
    unit            VARCHAR(20),
    price           NUMERIC(12,2)            NOT NULL DEFAULT 0,
    description     TEXT,
    notes           TEXT,
    is_deleted      BOOLEAN                  NOT NULL DEFAULT false,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
);

-- ─── Batches (inventory lots) ─────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS public.batches (
    id              UUID                     NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    batch_number    VARCHAR(100)             NOT NULL,
    item_id         UUID                     NOT NULL REFERENCES public.items(id),
    warehouse_id    UUID                     NOT NULL REFERENCES public.lager(id),
    expiry_date     TIMESTAMP WITHOUT TIME ZONE,
    current_stock   NUMERIC(12,3)            NOT NULL DEFAULT 0,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
);

-- ─── Bills of material ────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS public.bills_of_material (
    id              UUID                     NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    item_id         UUID                     NOT NULL REFERENCES public.items(id),
    component_id    UUID                     NOT NULL REFERENCES public.items(id),
    quantity        NUMERIC(12,3)            NOT NULL,
    unit            VARCHAR(20),
    notes           TEXT,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
);

-- ─── Roles ───────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS public.roles (
    id              UUID                     NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    name            VARCHAR(100)             NOT NULL UNIQUE,
    description     TEXT,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
);

-- ─── Permissions ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS public.permissions (
    id              UUID                     NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    name            VARCHAR(100)             NOT NULL,
    codename        VARCHAR(100)             NOT NULL UNIQUE,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
);

-- ─── Role ↔ Permission join ───────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS public.role_permissions (
    role_id         UUID NOT NULL REFERENCES public.roles(id)       ON DELETE CASCADE,
    permission_id   UUID NOT NULL REFERENCES public.permissions(id)  ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- ─── Users ───────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS public.users (
    id              UUID                     NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    email           VARCHAR(255)             NOT NULL UNIQUE,
    password        VARCHAR(255)             NOT NULL,
    role_id         UUID                     REFERENCES public.roles(id),
    preferred_lang  VARCHAR(10)              NOT NULL DEFAULT 'de',
    is_active       BOOLEAN                  NOT NULL DEFAULT true,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMP WITHOUT TIME ZONE
);

-- ─── Audit logs ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS public.audit_logs (
    id              UUID                     NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id         UUID,
    entity_type     VARCHAR(100)             NOT NULL,
    entity_id       UUID,
    action          VARCHAR(50)              NOT NULL,
    old_value       TEXT,
    new_value       TEXT,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
);

-- ─── Grants ──────────────────────────────────────────────────────────────────
GRANT ALL ON TABLE public.items             TO anon, authenticated, service_role;
GRANT ALL ON TABLE public.batches           TO anon, authenticated, service_role;
GRANT ALL ON TABLE public.bills_of_material TO anon, authenticated, service_role;
GRANT ALL ON TABLE public.roles             TO anon, authenticated, service_role;
GRANT ALL ON TABLE public.permissions       TO anon, authenticated, service_role;
GRANT ALL ON TABLE public.role_permissions  TO anon, authenticated, service_role;
GRANT ALL ON TABLE public.users             TO anon, authenticated, service_role;
GRANT ALL ON TABLE public.audit_logs        TO anon, authenticated, service_role;

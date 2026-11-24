-- Stratiq Complete Database Schema
-- Combines all tables, enums, indexes for initial setup

-- ═══════════════════════════════════════════════════════════════════════════════
-- ENUMS
-- ═══════════════════════════════════════════════════════════════════════════════

CREATE TYPE invoice_status_enum AS ENUM (
    'ISSUED',
    'PAID',
    'CANCELLED'
);

CREATE TYPE order_status_enum AS ENUM (
    'DRAFT',
    'CONFIRMED',
    'CANCELLED',
    'COMPLETED'
);

CREATE TYPE production_step_status_enum AS ENUM (
    'PENDING',
    'IN_PROGRESS',
    'DONE'
);

CREATE TYPE unit_enum AS ENUM (
    'piece',
    'kg',
    'g',
    'l',
    'ml',
    'm',
    'cm',
    'm2',
    'm3',
    'hour',
    'day',
    'set'
);

CREATE TYPE item_kind_enum AS ENUM ('OWN_PRODUCT', 'PURCHASED');

-- ═══════════════════════════════════════════════════════════════════════════════
-- CORE TABLES
-- ═══════════════════════════════════════════════════════════════════════════════

-- ─── Category ────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS category (
    id UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    description VARCHAR(255),
    name VARCHAR(50)
);

-- ─── Asset ───────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS asset (
    id UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    name VARCHAR(255),
    category_id UUID REFERENCES category(id)
);

-- ─── Kunde (Customer) ────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS kunde (
    id UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    name VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(50),
    tax_id VARCHAR(50),
    street VARCHAR(255),
    house_number VARCHAR(20),
    zip_code VARCHAR(20),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    notes TEXT,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    deleted_at TIMESTAMP WITHOUT TIME ZONE
);

-- ─── Lager (Warehouse) ───────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS lager (
    id UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    name VARCHAR(255),
    description TEXT,
    street VARCHAR(255),
    house_number VARCHAR(20),
    zip_code VARCHAR(20),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT true,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    deleted_at TIMESTAMP WITHOUT TIME ZONE
);

-- ─── Auftrag (Order) ─────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS auftrag (
    id UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    kunden_id UUID REFERENCES kunde(id),
    order_number VARCHAR(50),
    status order_status_enum NOT NULL DEFAULT 'DRAFT',
    order_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    expected_date TIMESTAMP WITHOUT TIME ZONE,
    confirmed_at TIMESTAMP WITHOUT TIME ZONE,
    cancelled_at TIMESTAMP WITHOUT TIME ZONE,
    total_amount NUMERIC(14,2) NOT NULL DEFAULT 0,
    notes TEXT,
    created_by UUID,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    deleted_at TIMESTAMP WITHOUT TIME ZONE
);

-- ─── Auftrag Assets (Order Items) ────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS auftrag_assets (
    id UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    auftrag_id UUID NOT NULL REFERENCES auftrag(id),
    asset_id UUID NOT NULL REFERENCES asset(id),
    item_name VARCHAR(255) NOT NULL,
    unit unit_enum NOT NULL,
    quantity NUMERIC(12,3) NOT NULL,
    unit_price NUMERIC(12,2) NOT NULL,
    line_total NUMERIC(14,2) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    supplier_id UUID
);

-- ─── Suppliers ───────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS suppliers (
    id UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(50),
    tax_id VARCHAR(50),
    street VARCHAR(255),
    house_number VARCHAR(20),
    zip_code VARCHAR(20),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    deleted_at TIMESTAMP WITHOUT TIME ZONE
);

-- Add supplier FK to auftrag_assets
ALTER TABLE auftrag_assets ADD CONSTRAINT fk_auftrag_assets_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id);

-- ─── Stock Movement ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS stock_movement (
    id UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    note VARCHAR(255),
    quantity BIGINT,
    type VARCHAR(255),
    asset_id UUID REFERENCES asset(id),
    auftrag_id UUID REFERENCES auftrag(id),
    lager_id UUID REFERENCES lager(id)
);

-- ─── Invoices ────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS invoices (
    id UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    auftrag_id UUID NOT NULL REFERENCES auftrag(id),
    status invoice_status_enum NOT NULL DEFAULT 'ISSUED',
    issue_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    due_date TIMESTAMP WITHOUT TIME ZONE,
    paid_at TIMESTAMP WITHOUT TIME ZONE,
    cancelled_at TIMESTAMP WITHOUT TIME ZONE,
    subtotal NUMERIC(14,2) NOT NULL DEFAULT 0,
    tax_amount NUMERIC(14,2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(14,2) NOT NULL DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
);

-- ─── Production Steps ────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS production_steps (
    id UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    asset_id UUID NOT NULL REFERENCES asset(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    step_order INTEGER NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    notes VARCHAR(255)
);

-- ─── Order Production Steps ──────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS order_production_steps (
    id UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    auftrag_id UUID NOT NULL REFERENCES auftrag(id),
    auftrag_asset_id UUID NOT NULL REFERENCES auftrag_assets(id),
    template_step_id UUID NOT NULL REFERENCES production_steps(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    step_order INTEGER NOT NULL,
    status production_step_status_enum NOT NULL DEFAULT 'PENDING',
    started_at TIMESTAMP WITHOUT TIME ZONE,
    completed_at TIMESTAMP WITHOUT TIME ZONE,
    assigned_to UUID,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
);

-- ═══════════════════════════════════════════════════════════════════════════════
-- INVENTORY TABLES
-- ═══════════════════════════════════════════════════════════════════════════════

-- ─── Items (product / article catalogue) ─────────────────────────────────────
CREATE TABLE IF NOT EXISTS items (
    id UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    kind item_kind_enum NOT NULL DEFAULT 'OWN_PRODUCT',
    category_id UUID REFERENCES category(id),
    unit VARCHAR(20),
    price NUMERIC(12,2) NOT NULL DEFAULT 0,
    description TEXT,
    notes TEXT,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
);

-- ─── Batches (inventory lots) ─────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS batches (
    id UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    batch_number VARCHAR(100) NOT NULL,
    item_id UUID NOT NULL REFERENCES items(id),
    warehouse_id UUID NOT NULL REFERENCES lager(id),
    expiry_date TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
);

-- ─── Bills of material ────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS bills_of_material (
    id UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    item_id UUID NOT NULL REFERENCES items(id),
    component_id UUID NOT NULL REFERENCES items(id),
    quantity NUMERIC(12,3) NOT NULL,
    unit VARCHAR(20),
    notes TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
);

-- ═══════════════════════════════════════════════════════════════════════════════
-- USER & RBAC TABLES
-- ═══════════════════════════════════════════════════════════════════════════════

-- ─── Roles ───────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS roles (
    id UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
);

-- ─── Permissions ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS permissions (
    id UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    codename VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
);

-- ─── Role ↔ Permission join ───────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- ─── Users (app_user) ────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS app_user (
    id UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role_id UUID REFERENCES roles(id),
    preferred_lang VARCHAR(10) NOT NULL DEFAULT 'de',
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    deleted_at TIMESTAMP WITHOUT TIME ZONE
);

-- ═══════════════════════════════════════════════════════════════════════════════
-- AUDIT & TRACKING TABLES
-- ═══════════════════════════════════════════════════════════════════════════════

-- ─── Audit logs ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID,
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID,
    action VARCHAR(50) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
);

-- ═══════════════════════════════════════════════════════════════════════════════
-- INDEXES FOR PERFORMANCE
-- ═══════════════════════════════════════════════════════════════════════════════

-- Core domain indexes
CREATE INDEX idx_asset_category_id ON asset(category_id);
CREATE INDEX idx_auftrag_kunden_id ON auftrag(kunden_id);
CREATE INDEX idx_auftrag_status ON auftrag(status);
CREATE INDEX idx_auftrag_assets_auftrag_id ON auftrag_assets(auftrag_id);
CREATE INDEX idx_auftrag_assets_asset_id ON auftrag_assets(asset_id);
CREATE INDEX idx_stock_movement_asset_id ON stock_movement(asset_id);
CREATE INDEX idx_stock_movement_lager_id ON stock_movement(lager_id);
CREATE INDEX idx_invoices_auftrag_id ON invoices(auftrag_id);
CREATE INDEX idx_production_steps_asset_id ON production_steps(asset_id);
CREATE INDEX idx_order_production_steps_auftrag_id ON order_production_steps(auftrag_id);

-- Inventory indexes
CREATE INDEX idx_items_category_id ON items(category_id);
CREATE INDEX idx_items_kind ON items(kind);
CREATE INDEX idx_batches_item_id ON batches(item_id);
CREATE INDEX idx_batches_warehouse_id ON batches(warehouse_id);
CREATE INDEX idx_bom_item_id ON bills_of_material(item_id);
CREATE INDEX idx_bom_component_id ON bills_of_material(component_id);

-- RBAC indexes
CREATE INDEX idx_role_permissions_role_id ON role_permissions(role_id);
CREATE INDEX idx_role_permissions_permission_id ON role_permissions(permission_id);
CREATE INDEX idx_app_user_email ON app_user(email);
CREATE INDEX idx_app_user_role_id ON app_user(role_id);

-- Audit indexes
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);

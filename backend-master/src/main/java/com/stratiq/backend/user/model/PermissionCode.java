package com.stratiq.backend.user.model;

// Single source of truth for permission codenames, mirrored in AdminSeeder.
// Give this list to the frontend team to gate features by authority string.
public enum PermissionCode {
    INVENTORY_READ("inventory:read", "View items, categories, suppliers, assets, batches and BOMs"),
    INVENTORY_WRITE("inventory:write", "Create and edit inventory data"),
    INVENTORY_DELETE("inventory:delete", "Delete inventory data"),
    ORDER_READ("order:read", "View orders and invoices"),
    ORDER_WRITE("order:write", "Create and edit orders and invoices"),
    ORDER_DELETE("order:delete", "Delete orders and invoices"),
    CUSTOMER_READ("customer:read", "View customers"),
    CUSTOMER_WRITE("customer:write", "Create and edit customers"),
    CUSTOMER_DELETE("customer:delete", "Delete customers"),
    WAREHOUSE_READ("warehouse:read", "View warehouses, stock levels and movements"),
    WAREHOUSE_WRITE("warehouse:write", "Book stock movements and edit warehouse data"),
    WAREHOUSE_DELETE("warehouse:delete", "Delete warehouse data"),
    PRODUCTION_READ("production:read", "View production steps and order progress"),
    PRODUCTION_WRITE("production:write", "Create and update production steps"),
    PRODUCTION_DELETE("production:delete", "Delete production steps"),
    USER_READ("user:read", "View users and roles"),
    USER_WRITE("user:write", "Create and edit users and roles"),
    USER_DELETE("user:delete", "Delete users"),
    AUDIT_READ("audit:read", "View audit logs"),
    ADMIN_ACCESS("admin:access", "Access system configuration and admin tools");

    private final String codename;
    private final String description;

    PermissionCode(String codename, String description) {
        this.codename = codename;
        this.description = description;
    }

    public String getCodename() {
        return codename;
    }

    public String getDescription() {
        return description;
    }
}

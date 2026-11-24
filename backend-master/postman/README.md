# Postman Collection — Stratiq Backend Local Testing

Ready-to-use Postman collection for testing the Stratiq REST API locally with JWT authentication.

## Files
- `Stratiq.postman_collection.json` — collection with 105 test requests (46 unique endpoints) organized by domain
- `Stratiq.postman_environment.json` — environment with `baseUrl`, JWT tokens, and ID variables

---

## QUICK START

### 1. Import Collection & Environment
- Open Postman
- Click `File → Import`
- Select `Stratiq.postman_collection.json`
- Import `Stratiq.postman_environment.json`
- Select **"Stratiq Local"** environment from dropdown (top-right)

### 2. Start Backend
- Open IntelliJ IDE
- Right-click `BackendApplication.java`
- Select `Run 'BackendApplication'`
- Wait for: `Started BackendApplication in X.XXXs`

### 3. Get JWT Token
- In Postman, go to **Auth** folder
- Click **Login** request
- Click **Send**
- Token auto-saves to `{{token}}` variable
- Credentials: `admin@stratiq.com` / `admin123`

### 4. Test Any Endpoint
- All endpoints inherit JWT token from collection
- Any request will automatically include `Authorization: Bearer {{token}}`
- Start testing any domain folder

---

## Authentication

**Type:** JWT (Bearer Token)
**Credentials:** 
- Username: `admin@stratiq.com`
- Password: `admin123`

**Token Lifecycle:**
- Access token expires in: 24 hours (86400000ms)
- Refresh token expires in: 7 days (604800000ms)
- Use **Auth → Refresh Token** to get new access token when expired

**How it works:**
1. Login endpoint returns `accessToken` and `refreshToken`
2. Postman automatically saves `accessToken` to `{{token}}` variable
3. All other requests use `Authorization: Bearer {{token}}`

---

## API Endpoints (46 unique)

**Complete endpoint list by domain:**

### Auth (3)
- `POST /api/auth/login` — Get JWT tokens (username, password)
- `POST /api/auth/refresh` — Refresh access token (refreshToken)
- `POST /api/auth/change-password` — Change password (currentPassword, newPassword, confirmNewPassword)

### Users (5)
- `GET /api/v1/users` — List all users (paginated)
- `POST /api/v1/users` — Create user (email, password, roleId, preferredLang)
- `GET /api/v1/users/{userId}` — Get user by ID
- `PUT /api/v1/users/{userId}` — Update user (email, roleId, preferredLang, isActive)
- `DELETE /api/v1/users/{userId}` — Delete user (soft delete)

### Roles (5)
- `GET /api/v1/roles` — List all roles
- `POST /api/v1/roles` — Create role (name, description)
- `GET /api/v1/roles/{roleId}` — Get role by ID
- `PUT /api/v1/roles/{roleId}` — Update role
- `DELETE /api/v1/roles/{roleId}` — Delete role
- `POST /api/v1/roles/{roleId}/permissions` — Assign permissions to role

### Orders (5)
- `GET /api/v1/orders` — List orders (paginated, optional: status filter)
- `POST /api/v1/orders` — Create order (orderNumber, status, orderDate, expectedDate, totalAmount)
- `GET /api/v1/orders/{orderId}` — Get order by ID
- `PUT /api/v1/orders/{orderId}` — Update order
- `DELETE /api/v1/orders/{orderId}` — Delete order (soft delete)

### Order Items (5)
- `GET /api/v1/order-items` — List order items (optional: orderId filter)
- `POST /api/v1/order-items` — Create order item (orderId, assetId, itemName, unit, quantity, unitPrice)
- `GET /api/v1/order-items/{orderItemId}` — Get order item by ID
- `PUT /api/v1/order-items/{orderItemId}` — Update order item
- `DELETE /api/v1/order-items/{orderItemId}` — Delete order item

### Invoices (5)
- `GET /api/v1/invoices` — List invoices (optional: orderId, status filters)
- `POST /api/v1/invoices` — Create invoice (invoiceNumber, orderId, status, issueDate, dueDate, totalAmount)
- `GET /api/v1/invoices/{invoiceId}` — Get invoice by ID
- `PUT /api/v1/invoices/{invoiceId}` — Update invoice
- `DELETE /api/v1/invoices/{invoiceId}` — Delete invoice

### Inventory - Items (5)
- `GET /api/v1/items` — List items (paginated)
- `POST /api/v1/items` — Create item (name, kind: OWN_PRODUCT|PURCHASED, categoryId, unit, price)
- `GET /api/v1/items/{itemId}` — Get item by ID
- `PUT /api/v1/items/{itemId}` — Update item
- `DELETE /api/v1/items/{itemId}` — Delete item

### Inventory - Batches (5)
- `GET /api/v1/batches` — List batches (paginated)
- `POST /api/v1/batches` — Create batch (batchNumber, itemId, warehouseId, expiryDate, currentStock)
- `GET /api/v1/batches/{batchId}` — Get batch by ID
- `PUT /api/v1/batches/{batchId}` — Update batch
- `DELETE /api/v1/batches/{batchId}` — Delete batch

### Inventory - BOM (3)
- `GET /api/v1/bom` — List bill of materials
- `POST /api/v1/bom` — Create BOM (itemId, componentId, quantity, unit)
- `DELETE /api/v1/bom/{bomId}` — Delete BOM

### Inventory - Categories (3)
- `GET /api/v1/categories` — List categories (paginated)
- `POST /api/v1/categories` — Create category (name, description)
- `DELETE /api/v1/categories/{categoryId}` — Delete category

### Inventory - Suppliers (3)
- `GET /api/v1/suppliers` — List suppliers (paginated, optional: name filter)
- `POST /api/v1/suppliers` — Create supplier (name, contactEmail, phone, address)
- `DELETE /api/v1/suppliers/{supplierId}` — Delete supplier

### Inventory - Assets (4)
- `GET /api/v1/assets` — List assets (optional: categoryId, name filters)
- `POST /api/v1/assets` — Create asset (name, description, categoryId, unit)
- `GET /api/v1/assets/{assetId}` — Get asset by ID
- `PUT /api/v1/assets/{assetId}` — Update asset

### Warehouse - Warehouses (4)
- `GET /api/v1/warehouses` — List warehouses (optional: city, active filters)
- `POST /api/v1/warehouses` — Create warehouse (name, city, address, capacity)
- `GET /api/v1/warehouses/{warehouseId}` — Get warehouse by ID
- `PUT /api/v1/warehouses/{warehouseId}` — Update warehouse

### Warehouse - Stock Movements (5)
- `GET /api/v1/stock-movements` — List movements (optional: assetId, warehouseId, orderId, type filters)
- `POST /api/v1/stock-movements` — Create movement (note, quantity, type: IN|OUT, assetId, warehouseId)
- `GET /api/v1/stock-movements/{movementId}` — Get movement by ID
- `PUT /api/v1/stock-movements/{movementId}` — Update movement
- `DELETE /api/v1/stock-movements/{movementId}` — Delete movement

### Warehouse - Stock (4)
- `GET /api/v1/stock` — Get stock info (optional: assetId, warehouseId filters)
- `GET /api/v1/stock/movements` — List all stock movements
- `GET /api/v1/stock/difference-report` — Stock difference report (optional: assetId, warehouseId)
- `POST /api/v1/stock/adjustment` — Stock adjustment

### Production - Steps (4)
- `GET /api/v1/production-steps` — List steps (optional: assetId filter)
- `POST /api/v1/production-steps` — Create step (assetId, name, stepOrder, description)
- `GET /api/v1/production-steps/{stepId}` — Get step by ID
- `PUT /api/v1/production-steps/{stepId}` — Update step

### Production - Order Steps (4)
- `GET /api/v1/order-production-steps` — List order steps (optional: orderId filter)
- `POST /api/v1/order-production-steps` — Create order step (orderId, orderItemId, templateStepId, name, stepOrder, status)
- `GET /api/v1/order-production-steps/{stepId}` — Get order step by ID
- `PUT /api/v1/order-production-steps/{stepId}` — Update order step

### Audit (1)
- `GET /api/v1/audit-logs` — View audit logs with filtering

### Permissions (2)
- `GET /api/v1/permissions` — List all permissions
- `POST /api/v1/permissions` — Create permission (name, codename)

---

## Environment Variables

### Auto-populated after Login:
- `{{token}}` — JWT access token (24h expiration)
- `{{refreshToken}}` — Refresh token (7d expiration)

### Base Configuration:
- `{{baseUrl}}` — http://localhost:8080

### Sample Test Data (auto-populated during collection run):
After running **Auth → Login**, subsequent requests will auto-populate:
- `{{assetId}}` — from Assets → Create response
- `{{categoryId}}` — from Categories → Create response
- `{{itemId}}` — from Items → Create response
- `{{warehouseId}}` — from Warehouses → Create response
- `{{orderId}}` — from Orders → Create response
- `{{orderItemId}}` — from Order Items → Create response
- `{{roleId}}` — from Roles → Create response
- `{{productionStepId}}` — from Production Steps → Create response

**Note:** These variables are optional—use real IDs from your test runs or set them manually in the environment editor.

---

## Testing Workflow

### Phase 1: Verify Backend (5 min)
1. Start backend in IntelliJ
2. Open http://localhost:8080/swagger-ui.html in browser
3. Verify Swagger UI loads and shows 46 endpoints

### Phase 2: Authentication (5 min)
1. Go to **Auth** folder → **Login**
2. Click **Send**
3. Verify response includes `accessToken` and `refreshToken`
4. Check `{{token}}` variable is populated

### Phase 3: List Operations (10 min)
1. Test **Users → List Users** (GET)
2. Test **Orders → List Orders** (GET)
3. Test **Inventory - Items → List Items** (GET)
4. Verify pagination works: `?page=0&size=10`

### Phase 4: Create Operations (10 min)
1. Create new **Category** (Inventory - Categories → Create)
2. Create new **Item** (Inventory - Items → Create)
3. Create new **Order** (Orders → Create)
4. Copy IDs from responses for next tests

### Phase 5: Update/Delete Operations (10 min)
1. Update created item (PUT /api/v1/items/{itemId})
2. Verify update with GET request
3. Delete created item (DELETE /api/v1/items/{itemId})
4. Verify soft delete (check deletedAt timestamp)

### Phase 6: Run Full Collection (10 min)
1. Click **Collections** → **Stratiq Backend API** → **Run**
2. Collection Runner opens
3. Click **Run Stratiq Backend API**
4. View pass/fail summary for all requests

---

## Pagination Parameters

All list endpoints support:
- `?page=0` — Page number (0-indexed)
- `&size=20` — Items per page
- `&sort=createdAt,desc` — Sort by field (asc/desc)

Examples:
```
GET /api/v1/orders?page=0&size=20&sort=createdAt,desc
GET /api/v1/users?page=1&size=10&sort=email,asc
```

---

## Response Status Codes

- `200 OK` — Successful GET/PUT/DELETE
- `201 Created` — Successful POST
- `204 No Content` — Successful DELETE (no body)
- `400 Bad Request` — Invalid input
- `401 Unauthorized` — Missing/invalid token
- `403 Forbidden` — Insufficient permissions
- `404 Not Found` — Resource not found
- `500 Internal Error` — Server error

---

## Common Issues & Solutions

### Login returns 400: "username: must not be blank"
**Cause:** Using `email` field instead of `username`

**Solution:** Use `username` field in login body:
```json
{
  "username": "admin@stratiq.com",
  "password": "admin123"
}
```

### 401 Unauthorized on protected endpoints
**Causes:**
- Token missing or expired (24h lifetime)
- Token format incorrect
- Token not sent in Authorization header

**Solutions:**
1. Run **Auth → Login** to get fresh token
2. Check `{{token}}` variable is populated (Postman)
3. Verify header: `Authorization: Bearer <token>`
4. Token expired? Use **Auth → Refresh Token**

### 400 Bad Request: Cannot deserialize enum
**Cause:** Wrong enum value (case-sensitive)

**Examples:**
- OrderStatus: `DRAFT`, `CONFIRMED`, `CANCELLED`, `COMPLETED` (not `PENDING`)
- Unit: `piece`, `kg`, `g`, `l`, `m`, `cm`, `hour`, `day`, `set` (lowercase)
- InvoiceStatus: `ISSUED`, `PAID`, `CANCELLED` (not `DRAFT`)
- ItemKind: `OWN_PRODUCT`, `PURCHASED`

**Solution:** Check exact enum values in API docs or error message

### 500 on POST /orders or /invoices
**Cause:** Invalid OrderStatus or InvoiceStatus enum

**Check field values:**
- Orders use: `DRAFT`, `CONFIRMED`, `CANCELLED`, `COMPLETED`
- Invoices use: `ISSUED`, `PAID`, `CANCELLED`

### 404 on GET with ID
**Causes:**
- ID doesn't exist
- ID is wrong (copy error)
- Resource was deleted (soft delete)

**Solutions:**
1. First run list endpoint to get valid IDs
2. Copy ID exactly from response
3. Check if resource was deleted (soft delete has deletedAt timestamp)

### 400 Bad Request on PUT /users
**Cause:** Missing required `isActive` field

**Solution:** Always include `isActive: true/false` in user updates:
```json
{
  "email": "user@test.com",
  "roleId": "ROLE_UUID",
  "preferredLang": "en",
  "isActive": true
}
```

### 500 on POST /batches
**Cause:** Missing required fields

**Solution:** Batch creation requires:
```json
{
  "batchNumber": "BATCH-001",
  "itemId": "ITEM_UUID",
  "warehouseId": "WAREHOUSE_UUID",
  "expiryDate": "2027-01-01T00:00:00",
  "currentStock": 100.0
}
```

### Collections runner shows failures
**Possible causes:**
- IDs don't exist (run create operations first)
- Enum values are incorrect
- Required fields are missing
- Token expired

**Solutions:**
1. Check collection execution order (Auth → Create operations → Get/Update)
2. Review failed request's error response
3. Verify environment variables are populated
4. Re-run Auth → Login to refresh token

---

## Example Workflows

### Create Order with Items
```
1. Auth → Login (get token)
2. Orders → Create Order (use {{customerId}})
3. Copy orderId from response
4. Order Items → Create Order Item (use orderId + {{assetId}})
5. Orders → Get Order (use orderId to verify items)
```

### Warehouse Stock Management
```
1. Auth → Login (get token)
2. Warehouse → Create Warehouse
3. Warehouse - Stock Movements → Create Stock Movement
   (use {{assetId}}, {{warehouseId}}, movementType: "IN", quantity: 100)
4. Warehouse - Stock → Get Stock Info (verify levels updated)
```

### User & Role Management
```
1. Auth → Login (get token)
2. Roles → Create Role (e.g., "MANAGER")
3. Users → Create User (assign MANAGER role)
4. Users → List Users (verify new user in list)
5. Users → Update User (change role to "ADMIN")
6. Users → Delete User (soft delete, check deletedAt)
```

---

## Running with curl

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin@stratiq.com","password":"admin123"}'
```

### Save token
```bash
export TOKEN="<accessToken from response>"
```

### List Users
```bash
curl -X GET http://localhost:8080/api/v1/users?page=0&size=20 \
  -H "Authorization: Bearer $TOKEN"
```

### Create Order
```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "orderNumber":"ORD-TEST-001",
    "status":"DRAFT",
    "orderDate":"2026-05-27T00:00:00",
    "expectedDate":"2026-06-27T00:00:00",
    "totalAmount":500.00,
    "notes":"Test order"
  }'
```

### Get Order
```bash
curl -X GET http://localhost:8080/api/v1/orders/YOUR_ORDER_ID \
  -H "Authorization: Bearer $TOKEN"
```

### List Orders with Status Filter
```bash
curl -X GET "http://localhost:8080/api/v1/orders?status=DRAFT" \
  -H "Authorization: Bearer $TOKEN"
```

### Update Order
```bash
curl -X PUT http://localhost:8080/api/v1/orders/YOUR_ORDER_ID \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "orderNumber":"ORD-TEST-001",
    "status":"CONFIRMED",
    "orderDate":"2026-05-27T00:00:00",
    "expectedDate":"2026-06-27T00:00:00",
    "totalAmount":500.00
  }'
```

### Delete Order
```bash
curl -X DELETE http://localhost:8080/api/v1/orders/YOUR_ORDER_ID \
  -H "Authorization: Bearer $TOKEN"
```

### Create Item
```bash
curl -X POST http://localhost:8080/api/v1/items \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name":"Steel Rod 10mm",
    "kind":"PURCHASED",
    "categoryId":"YOUR_CATEGORY_ID",
    "unit":"kg",
    "price":15.50
  }'
```

### Create Stock Movement
```bash
curl -X POST http://localhost:8080/api/v1/stock-movements \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "note":"Initial stock in",
    "quantity":100,
    "type":"IN",
    "assetId":"YOUR_ASSET_ID",
    "warehouseId":"YOUR_WAREHOUSE_ID"
  }'
```

---

## Quick Reference: Field Names & Types

### Enums (Case-sensitive)
- **OrderStatus:** `DRAFT`, `CONFIRMED`, `CANCELLED`, `COMPLETED`
- **InvoiceStatus:** `ISSUED`, `PAID`, `CANCELLED`
- **Unit (lowercase):** `piece`, `kg`, `g`, `l`, `ml`, `m`, `cm`, `m2`, `m3`, `hour`, `day`, `set`
- **ItemKind:** `OWN_PRODUCT`, `PURCHASED`
- **MovementType:** `IN`, `OUT`
- **ProductionStepStatus:** `PENDING`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`

### Common Required Fields
**Order Create:**
```json
{
  "orderNumber": "string (50 char max)",
  "status": "DRAFT|CONFIRMED|CANCELLED|COMPLETED",
  "orderDate": "ISO 8601",
  "expectedDate": "ISO 8601",
  "totalAmount": "decimal"
}
```

**Order Item Create:**
```json
{
  "orderId": "UUID",
  "assetId": "UUID",
  "itemName": "string (255 char max)",
  "unit": "piece|kg|...",
  "quantity": "decimal >= 0.001",
  "unitPrice": "decimal >= 0.00"
}
```

**Invoice Create:**
```json
{
  "invoiceNumber": "string (50 char max)",
  "orderId": "UUID",
  "status": "ISSUED|PAID|CANCELLED",
  "issueDate": "ISO 8601",
  "dueDate": "ISO 8601",
  "totalAmount": "decimal"
}
```

**Item Create:**
```json
{
  "name": "string",
  "kind": "OWN_PRODUCT|PURCHASED",
  "categoryId": "UUID",
  "unit": "piece|kg|...",
  "price": "decimal",
  "description": "string (optional)"
}
```

## Additional Resources

- **Live Backend:** http://localhost:8080
- **OpenAPI Docs:** http://localhost:8080/v3/api-docs (JSON)
- **Swagger UI:** http://localhost:8080/swagger-ui.html (Web UI)
- **Backend Health:** http://localhost:8080/actuator/health

---

**Testing Status:** All 46 endpoints tested and verified operational (May 27, 2026)
- **Swagger UI:** 41 paths, 86 operations visible
- **Auth:** Login, token refresh, password change working
- **CRUD:** All domains support create, read, update, delete
- **Filters:** List endpoints support domain-specific filters
- **Database:** Supabase PostgreSQL connected and operational

# Stratiq Backend

Production-ready REST API for the **Stratiq** inventory & order management system.  
Built with **Spring Boot 4**, **Java 25**, **PostgreSQL (Supabase)**, and stateless **JWT** authentication.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 4.0.5 |
| Security | Spring Security + JWT HS512 (stateless) |
| Persistence | Spring Data JPA + Hibernate 7 + PostgreSQL 17 |
| Database | Supabase PostgreSQL (remote) |
| Schema | Flyway migrations (`src/main/resources/db/migration/`) |
| API Docs | springdoc-openapi 3 (Swagger UI) |
| Validation | Jakarta Bean Validation |
| Mapping | Manual DTO ↔ Entity mappers |
| Audit | AOP-based audit logging (`@Aspect`) |
| Build | Maven 3 + `mvnw` wrapper |

---

## Architecture

Domain-based (vertical-slice) modular structure — each business domain owns its controller, service, repository, model, DTO, and mapper.

```
src/main/java/com/stratiq/backend/
├── auth/                    # JwtService, JwtAuthFilter, AuthController
├── shared/
│   ├── config/              # SecurityConfig, OpenApiConfig
│   ├── exception/           # GlobalExceptionHandler, NotFoundException
│   ├── model/               # Shared enums (Unit, ItemKind, OrderStatus, …)
│   ├── migration/           # SchemaRunner (applies db/migration on startup)
│   └── seeder/              # AdminSeeder (default profile), DataSeeder (seeder profile)
├── order/                   # Orders, OrderItems, Invoices
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── model/
│   ├── dto/
│   └── mapper/
├── inventory/               # Items, Batches, BillOfMaterial, Suppliers
├── warehouse/               # Warehouses, StockMovements, StockService
├── production/              # ProductionSteps, OrderProductionSteps
├── asset/                   # Assets (used across order, inventory, production)
├── category/                # Categories (shared by Asset & Item)
├── user/                    # AppUser, Role, Permission (RBAC)
└── audit/                   # AuditLog, AuditService
```

---

## Domain Entities

| Entity | Table | Domain |
|---|---|---|
| `AppUser` | `app_user` | user |
| `Role` | `roles` | user |
| `Permission` | `permissions` | user |
| `Category` | `category` | category |
| `Asset` | `asset` | asset |
| `Item` | `items` | inventory |
| `Batch` | `batches` | inventory |
| `BillOfMaterial` | `bills_of_material` | inventory |
| `Supplier` | `suppliers` | inventory |
| `Customer` | `kunde` | order |
| `Order` | `auftrag` | order |
| `OrderItem` | `auftrag_assets` | order |
| `Invoice` | `invoices` | order |
| `Warehouse` | `lager` | warehouse |
| `StockMovement` | `stock_movement` | warehouse |
| `ProductionStep` | `production_steps` | production |
| `OrderProductionStep` | `order_production_steps` | production |
| `AuditLog` | `audit_logs` | audit |

---

## Prerequisites

- Java 25
- Maven 3.9+ (or use `./mvnw`)
- PostgreSQL / Supabase project

---

## Configuration

Edit `src/main/resources/application.yaml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://<host>:5432/postgres?sslmode=require&stringtype=unspecified
    username: <user>
    password: <password>
  jpa:
    hibernate:
      ddl-auto: none
  flyway:
    enabled: false      # Schema applied via SchemaRunner on startup

app:
  jwt:
    secret: <min-256-bit-secret>
    expiration-ms: 86400000        # 24 hours
    refresh-expiration-ms: 604800000   # 7 days
```

> ⚠️ Never commit real credentials. Use environment variables or a secrets manager in production.

---

## Database Schema

Schema is managed via SQL in `src/main/resources/db/migration/`:

| File | Contents |
|---|---|
| `V1__Complete_schema.sql` | All enums, tables, foreign keys, and indexes in one file |

**Tables created:** `category`, `asset`, `kunde`, `lager`, `auftrag`, `auftrag_assets`, `suppliers`, `stock_movement`, `invoices`, `production_steps`, `order_production_steps`, `items`, `batches`, `bills_of_material`, `roles`, `permissions`, `role_permissions`, `app_user`, `audit_logs`

`SchemaRunner` applies the SQL automatically on first startup. Flyway is present in the classpath but disabled — `SchemaRunner` is the migration mechanism.

Supabase source migrations (for reference) are in `supabase/migrations/`.

---

## Running Locally

### IntelliJ IDEA (recommended)
1. Open the project
2. Right-click `BackendApplication.java` → **Run**
3. Wait for: `Started BackendApplication in X.XXXs`
4. Watch for: `Admin user created: admin@stratiq.com` (first run only)

### Maven CLI
```bash
./mvnw spring-boot:run
```

### Docker
```bash
docker compose up --build
```

App starts at **http://localhost:8080**

---

## Authentication

All `/api/v1/**` endpoints require a **JWT Bearer token**. The `AdminSeeder` creates `admin@stratiq.com` / `admin123` on first startup.

### 1. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin@stratiq.com","password":"admin123"}'
```

Response:
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "username": "admin@stratiq.com"
}
```

### 2. Use the token
```bash
export TOKEN="<accessToken>"

curl http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer $TOKEN"
```

### 3. Refresh
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"eyJ..."}'
```

Token expiry: **24 hours** (access) · **7 days** (refresh)

---

## REST API

**Base path:** `/api/v1`

### Endpoints

| Domain | Method | Path | Description |
|---|---|---|---|
| **Auth** | POST | `/api/auth/login` | Get JWT tokens |
| | POST | `/api/auth/refresh` | Refresh access token |
| | POST | `/api/auth/change-password` | Change password |
| **Users** | GET | `/api/v1/users` | List users (paginated) |
| | POST | `/api/v1/users` | Create user |
| | GET | `/api/v1/users/{id}` | Get user |
| | PUT | `/api/v1/users/{id}` | Update user |
| | DELETE | `/api/v1/users/{id}` | Delete user (soft) |
| **Roles** | GET | `/api/v1/roles` | List roles |
| | POST | `/api/v1/roles` | Create role |
| | GET | `/api/v1/roles/{id}` | Get role |
| | PUT | `/api/v1/roles/{id}` | Update role |
| | DELETE | `/api/v1/roles/{id}` | Delete role |
| **Permissions** | GET | `/api/v1/permissions` | List permissions |
| | POST | `/api/v1/permissions` | Create permission |
| | POST | `/api/v1/permissions/assign` | Assign to role |
| | DELETE | `/api/v1/permissions/{id}` | Delete permission |
| **Orders** | GET | `/api/v1/orders` | List orders (paginated) |
| | POST | `/api/v1/orders` | Create order |
| | GET | `/api/v1/orders/{id}` | Get order |
| | PUT | `/api/v1/orders/{id}` | Update order |
| | DELETE | `/api/v1/orders/{id}` | Delete order (soft) |
| **Order Items** | GET | `/api/v1/order-items` | List order items |
| | POST | `/api/v1/order-items` | Create order item |
| | GET | `/api/v1/order-items/{id}` | Get order item |
| | PUT | `/api/v1/order-items/{id}` | Update order item |
| | DELETE | `/api/v1/order-items/{id}` | Delete order item |
| **Invoices** | GET | `/api/v1/invoices` | List invoices (paginated) |
| | POST | `/api/v1/invoices` | Create invoice |
| | GET | `/api/v1/invoices/{id}` | Get invoice |
| | PUT | `/api/v1/invoices/{id}` | Update invoice |
| | DELETE | `/api/v1/invoices/{id}` | Delete invoice |
| **Items** | GET | `/api/v1/items` | List items (paginated) |
| | POST | `/api/v1/items` | Create item |
| | GET | `/api/v1/items/{id}` | Get item |
| | PUT | `/api/v1/items/{id}` | Update item |
| **Batches** | GET | `/api/v1/batches` | List batches (paginated) |
| | POST | `/api/v1/batches` | Create batch |
| | GET | `/api/v1/batches/{id}` | Get batch |
| | PUT | `/api/v1/batches/{id}` | Update batch |
| **BOM** | GET | `/api/v1/bom` | List bills of material |
| | POST | `/api/v1/bom` | Create BOM entry |
| **Categories** | GET | `/api/v1/categories` | List categories (paginated) |
| | POST | `/api/v1/categories` | Create category |
| **Suppliers** | GET | `/api/v1/suppliers` | List suppliers (paginated) |
| | POST | `/api/v1/suppliers` | Create supplier |
| **Assets** | GET | `/api/v1/assets` | List assets (paginated) |
| | POST | `/api/v1/assets` | Create asset |
| **Warehouses** | GET | `/api/v1/warehouses` | List warehouses (paginated) |
| | POST | `/api/v1/warehouses` | Create warehouse |
| | GET | `/api/v1/warehouses/{id}` | Get warehouse |
| **Stock Movements** | GET | `/api/v1/stock-movements` | List movements (paginated) |
| | POST | `/api/v1/stock-movements` | Create movement |
| | GET | `/api/v1/stock-movements/{id}` | Get movement |
| **Stock** | GET | `/api/v1/stock` | Current stock summary |
| | GET | `/api/v1/stock/difference-report` | Stock difference report |
| **Production Steps** | GET | `/api/v1/production-steps` | List steps (paginated) |
| | POST | `/api/v1/production-steps` | Create step |
| | GET | `/api/v1/production-steps/{id}` | Get step |
| **Order Prod. Steps** | GET | `/api/v1/order-production-steps` | List order steps |
| | POST | `/api/v1/order-production-steps` | Create order step |
| | GET | `/api/v1/order-production-steps/{id}` | Get order step |
| **Audit** | GET | `/api/v1/audit-logs` | List audit logs (paginated) |

### Pagination
```
GET /api/v1/orders?page=0&size=20&sort=createdAt,desc
```

| Param | Default | Description |
|---|---|---|
| `page` | 0 | Zero-based page index |
| `size` | 20 | Items per page |
| `sort` | — | `field,direction` e.g. `createdAt,desc` |

### Enums

| Field | Values |
|---|---|
| `OrderStatus` | `DRAFT` `CONFIRMED` `CANCELLED` `COMPLETED` |
| `InvoiceStatus` | `ISSUED` `PAID` `CANCELLED` |
| `ItemKind` | `OWN_PRODUCT` `PURCHASED` |
| `ProductionStepStatus` | `PENDING` `IN_PROGRESS` `DONE` |
| `StockMovement.type` | `IN` `OUT` `TRANSFER` |
| `Unit` | `piece` `kg` `g` `l` `ml` `m` `cm` `m2` `m3` `hour` `day` `set` |

---

## API Documentation (Swagger UI)

- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

Click **Authorize**, paste the JWT access token — all requests in the UI will include the Bearer header automatically.

---

## Error Responses

All errors follow a uniform JSON format:

```json
{
  "timestamp": "2026-05-27T10:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Order not found: 00000000-0000-0000-0000-000000000000",
  "path": "/api/v1/orders/00000000-0000-0000-0000-000000000000"
}
```

| Exception | HTTP Status |
|---|---|
| `NotFoundException` | 404 |
| `MethodArgumentNotValidException` | 400 |
| `IllegalArgumentException` | 400 |
| `DataIntegrityViolationException` | 409 |
| `AccessDeniedException` | 403 |
| Fallback | 500 |

---

## Postman

A complete collection is in `postman/` — see `postman/README.md` for full instructions.

| File | Description |
|---|---|
| `Stratiq.postman_collection.json` | All endpoints with request bodies and pagination params |
| `Stratiq.postman_environment.json` | Pre-filled environment variables (baseUrl, token, test UUIDs) |

**Quick start:**
1. Import both files into Postman
2. Select **Stratiq Local** environment
3. Run **Auth → Login** — `{{token}}` saves automatically
4. All other requests inherit Bearer auth from the collection

---

## Tests

**409 tests, 0 failures** across all domains.

```bash
# Run all tests
./mvnw test

# Run by domain
./mvnw test -Dtest="**/order/**"
./mvnw test -Dtest="**/inventory/**"

# Run by type
./mvnw test -Dtest="*ServiceTest"
./mvnw test -Dtest="*ControllerTest"
./mvnw test -Dtest="*MapperTest"

# Skip tests during build
./mvnw clean package -DskipTests
```

### Coverage

| Type | What is tested |
|---|---|
| Controller (`@WebMvcTest`) | Routing, JWT auth enforcement, `@PreAuthorize` per authority, 403 for unauthenticated |
| Service (`MockitoExtension`) | Business logic, `NotFoundException`, CRUD, soft-delete |
| Mapper (plain JUnit 5) | Field mapping, default values, null-safe navigation, selective update null-checks |
| Auth | `JwtAuthFilter` branches, `JwtService` token lifecycle, `AppUserDetailsService` |
| Shared | All 6 `GlobalExceptionHandler` handlers |

See [`src/test/java/com/stratiq/backend/README.md`](src/test/java/com/stratiq/backend/README.md) for the full test structure.

---

## Build

```bash
# Fat JAR
./mvnw clean package -DskipTests

# Run JAR
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

---

## Git

- **Branch:** `new-implement`
- **Remote:** `https://git.thm.de/stratiq/backend.git`

---

*Industry 4.0 Project — THM Giessen, 2026*

---

## Test Coverage Summary

| Domain | Tests |
|---|---|
| auth | 27 |
| audit | 17 |
| inventory | 130 |
| order | 97 |
| production | 44 |
| user | 49 |
| warehouse | 64 |
| shared | 6 |
| **Total** | **409** |

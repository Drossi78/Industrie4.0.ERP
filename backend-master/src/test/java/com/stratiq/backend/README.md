# Stratiq Backend — Test Suite

JUnit 5 test suite for the Stratiq backend. **409 tests, 0 failures.**

---

## Run Tests

```bash
# All tests
./mvnw test

# Single class
./mvnw test -Dtest=OrderServiceTest

# By pattern
./mvnw test -Dtest="*ServiceTest"
./mvnw test -Dtest="*ControllerTest"
./mvnw test -Dtest="*MapperTest"

# By package
./mvnw test -Dtest="**/order/**"
./mvnw test -Dtest="**/inventory/**"

# Skip tests during build
./mvnw clean package -DskipTests
```

---

## Test Structure

```
src/test/java/com/stratiq/backend/
├── auth/
│   ├── AuthControllerTest.java          # Login, refresh, change-password endpoints
│   ├── AppUserDetailsServiceTest.java   # loadUserByUsername — found / not found
│   ├── JwtServiceTest.java              # Token generation, validation, access vs refresh
│   └── JwtAuthFilterTest.java           # Filter branches: no header, valid, invalid token
├── audit/
│   ├── controller/AuditControllerTest.java
│   ├── service/AuditServiceTest.java
│   └── mapper/AuditLogMapperTest.java
├── inventory/
│   ├── controller/                      # AssetControllerTest, BatchControllerTest,
│   │                                    # BomControllerTest, CategoryControllerTest,
│   │                                    # ItemControllerTest, SupplierControllerTest
│   ├── service/                         # AssetServiceTest, BatchServiceTest,
│   │                                    # BomServiceTest, CategoryServiceTest,
│   │                                    # ItemServiceTest, SupplierServiceTest
│   └── mapper/                          # AssetMapperTest, BatchMapperTest, BomMapperTest,
│                                        # CategoryMapperTest, ItemMapperTest, SupplierMapperTest
├── order/
│   ├── controller/                      # CustomerControllerTest, InvoiceControllerTest,
│   │                                    # OrderControllerTest, OrderItemControllerTest
│   ├── service/                         # CustomerServiceTest, InvoiceServiceTest,
│   │                                    # InvoicePdfServiceTest, OrderServiceTest,
│   │                                    # OrderItemServiceTest
│   └── mapper/                          # CustomerMapperTest, InvoiceMapperTest,
│                                        # OrderMapperTest, OrderItemMapperTest
├── production/
│   ├── controller/                      # OrderProductionStepControllerTest,
│   │                                    # ProductionStepControllerTest
│   ├── service/                         # OrderProductionStepServiceTest,
│   │                                    # ProductionStepServiceTest
│   └── mapper/                          # OrderProductionStepMapperTest,
│                                        # ProductionStepMapperTest
├── shared/
│   └── exception/GlobalExceptionHandlerTest.java  # All 6 exception handlers
├── user/
│   ├── controller/                      # PermissionControllerTest, RoleControllerTest,
│   │                                    # UserControllerTest
│   ├── service/                         # PermissionServiceTest, RoleServiceTest,
│   │                                    # UserServiceTest
│   └── mapper/                          # RoleMapperTest, UserMapperTest
└── warehouse/
    ├── controller/                      # StockControllerTest, StockMovementControllerTest,
    │                                    # WarehouseControllerTest
    ├── service/                         # StockMovementServiceTest, StockServiceTest,
    │                                    # WarehouseServiceTest
    └── mapper/                          # StockMovementMapperTest, WarehouseMapperTest
```

---

## Test Types

### Controller Tests (`@WebMvcTest`)
Test HTTP layer in isolation: routing, auth enforcement, request/response format.

```java
@Import(SecurityConfig.class)
@WebMvcTest(excludeAutoConfiguration = {UserDetailsServiceAutoConfiguration.class},
            value = OrderController.class)
class OrderControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean OrderService orderService;
    @MockitoBean JwtService jwtService;
    @MockitoBean AppUserDetailsService appUserDetailsService;

    @Test
    void listOrders_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/orders"))
               .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "order:read")
    void listOrders_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/orders"))
               .andExpect(status().isOk());
    }
}
```

**Key conventions:**
- Unauthenticated requests → `403 Forbidden` (stateless JWT uses `Http403ForbiddenEntryPoint`)
- Authority-based: `@WithMockUser(authorities = "order:read")`
- Wrong authority → `403` via `@PreAuthorize` + `AccessDeniedException` handler
- `ObjectMapper` instantiated directly (`new ObjectMapper()`), not `@Autowired`

### Service Tests (`@ExtendWith(MockitoExtension.class)`)
Test business logic in isolation using Mockito.

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock OrderRepository orderRepository;
    @Mock OrderMapper orderMapper;
    @InjectMocks OrderServiceImpl orderService;

    @Test
    void create_savesAndReturnsResponse() {
        // Arrange, Act, Assert
    }

    @Test
    void get_notFound_throwsNotFoundException() {
        when(orderRepository.findById(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> orderService.get(UUID.randomUUID()))
            .isInstanceOf(NotFoundException.class);
    }
}
```

### Mapper Tests (plain JUnit 5)
Test field mapping, default values, null-safe navigation, selective update logic.

```java
class OrderMapperTest {
    private final OrderMapper mapper = new OrderMapper();

    @Test
    void toEntity_withNullStatus_defaultsToDraft() {
        Order result = mapper.toEntity(req, customer);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.DRAFT);
    }

    @Test
    void update_withNullFields_keepsExistingValues() {
        // Verify null fields in request don't overwrite existing entity values
    }
}
```

---

## Test Count by Domain

| Domain | Controllers | Services | Mappers | Auth/Shared | Total |
|--------|-------------|----------|---------|-------------|-------|
| auth | 17 | 10 | — | — | 27 |
| audit | 9 | 7 | 1 | — | 17 |
| inventory | 55 | 53 | 22 | — | 130 |
| order | 40 | 37 | 20 | — | 97 |
| production | 16 | 18 | 10 | — | 44 |
| user | 21 | 23 | 5 | — | 49 |
| warehouse | 27 | 26 | 11 | — | 64 |
| shared | — | — | — | 6 | 6 |
| **Total** | | | | | **409** |

---

## Naming Convention

Format: `methodOrScenario_condition_expectedResult`

```
listOrders_unauthenticated_returns403
getOrder_notFound_returns404
createOrder_returnsCreated
toEntity_withNullStatus_defaultsToDraft
update_withNullFields_keepsExistingValues
```

---

## Security in Tests

`SecurityConfig` is imported directly in all controller tests:

```java
@Import(SecurityConfig.class)
@WebMvcTest(excludeAutoConfiguration = {UserDetailsServiceAutoConfiguration.class}, ...)
```

This ensures:
- CSRF disabled (allows POST/PUT/DELETE in tests)
- `permitAll` on `/api/auth/**`
- `@PreAuthorize` enforced via `@EnableMethodSecurity`
- Unauthenticated → 403 (not 401) because of stateless JWT config

---

**Last updated:** 2026-06-08  
**Test count:** 409 · **Failures:** 0 · **Errors:** 0

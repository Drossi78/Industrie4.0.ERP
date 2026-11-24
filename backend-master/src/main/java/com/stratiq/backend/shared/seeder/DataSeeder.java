package com.stratiq.backend.shared;

import com.stratiq.backend.inventory.model.Category;
import com.stratiq.backend.order.model.Customer;
import com.stratiq.backend.order.model.Order;
import com.stratiq.backend.order.model.OrderStatus;
import com.stratiq.backend.inventory.model.Item;
import com.stratiq.backend.warehouse.model.StockMovement;
import com.stratiq.backend.warehouse.model.Warehouse;
import com.stratiq.backend.inventory.repository.CategoryRepository;
import com.stratiq.backend.order.repository.CustomerRepository;
import com.stratiq.backend.order.repository.OrderRepository;
import com.stratiq.backend.inventory.repository.ItemRepository;
import com.stratiq.backend.warehouse.repository.StockMovementRepository;
import com.stratiq.backend.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Component
@Profile("seeder")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ItemRepository itemRepository;
    private final WarehouseRepository warehouseRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final StockMovementRepository stockMovementRepository;

    @Override
    public void run(String... args) {
        log.info("start db-test seeding");

        // Category
        Category category = new Category();
        category.setName("Tools");
        category.setDescription("Hand and machine tools");
        categoryRepository.save(category);
        log.info("category created: {}", category.getId());

        // Item
        Item item = new Item();
        item.setName("Test-Item");
        item.setCategory(category);
        itemRepository.save(item);
        log.info("item created: {} ({})", item.getName(), item.getId());

        // Warehouse
        Warehouse warehouse = new Warehouse();
        warehouse.setName("Main Warehouse");
        warehouse.setCity("Frankfurt");
        warehouse.setCountry("Germany");
        warehouse.setActive(true);
        warehouse.setUpdatedAt(LocalDateTime.now());
        warehouseRepository.save(warehouse);
        log.info("warehouse created: {} ({})", warehouse.getName(), warehouse.getId());

        // Customer
        Customer customer = new Customer();
        customer.setName("John Doe");
        customer.setEmail("john@example.com");
        customer.setCity("Berlin");
        customer.setCountry("Germany");
        customer.setUpdatedAt(LocalDateTime.now());
        customerRepository.save(customer);
        log.info("customer created: {} ({})", customer.getName(), customer.getId());

        // Order
        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderNumber("ORD-001");
        order.setStatus(OrderStatus.DRAFT);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(BigDecimal.ZERO);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
        log.info("order created: {} (Customer: {})", order.getId(), customer.getName());

        // Stock Movement
        StockMovement movement = new StockMovement();
        movement.setType("IN");
        movement.setQuantity(42L);
        movement.setNote("Test delivery");
        movement.setItem(item);
        movement.setWarehouse(warehouse);
        movement.setOrder(order);
        stockMovementRepository.save(movement);
        log.info("stock movement created: {} (Type: {}, Qty: {})",
                movement.getId(), movement.getType(), movement.getQuantity());

        log.info("test-seeding done");
    }
}

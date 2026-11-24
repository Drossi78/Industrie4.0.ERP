package com.stratiq.backend.order.repository;

import com.stratiq.backend.order.model.Order;
import com.stratiq.backend.order.model.OrderDirection;
import com.stratiq.backend.order.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    // Retrieve all orders associated with a specific customer.
    List<Order> findByCustomerId(UUID customerId);
    // Filter orders by their current status (DRAFT, CONFIRMED, SHIPPED, DELIVERED, CANCELLED).
    List<Order> findByStatus(OrderStatus status);
    // Filter orders by their stock direction (IN = goods receipt, OUT = goods issue).
    List<Order> findByDirection(OrderDirection direction);
    // Soft-delete query: only returns orders not marked as deleted (deletedAt is null).
    List<Order> findByDeletedAtIsNull();
}

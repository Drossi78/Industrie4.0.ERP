package com.stratiq.backend.order.repository;

import com.stratiq.backend.order.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
    // Retrieve all line items for a specific order (order detail rows).
    List<OrderItem> findByOrderId(UUID orderId);
    // Find all order items referencing a specific item (item usage across orders).
    List<OrderItem> findByItemId(UUID itemId);
    // Find order items from a specific supplier (supplier order volume analysis).
    List<OrderItem> findBySupplierId(UUID supplierId);
}

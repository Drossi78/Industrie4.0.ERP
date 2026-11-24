package com.stratiq.backend.order.mapper;

import com.stratiq.backend.order.dto.request.OrderCreateRequest;
import com.stratiq.backend.order.dto.request.OrderUpdateRequest;
import com.stratiq.backend.order.dto.response.OrderResponse;
import com.stratiq.backend.order.model.Customer;
import com.stratiq.backend.order.model.Order;
import com.stratiq.backend.order.model.OrderDirection;
import com.stratiq.backend.order.model.OrderStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class OrderMapper {

    public Order toEntity(OrderCreateRequest req, Customer customer) {
        Order o = new Order();
        o.setCustomer(customer);
        o.setOrderNumber(req.orderNumber());
        // Defaults: DRAFT status, current timestamp for orderDate, zero amount if null.
        o.setStatus(req.status() != null ? req.status() : OrderStatus.DRAFT);
        // Direction defaults to OUT (sale) when not specified.
        o.setDirection(req.direction() != null ? req.direction() : OrderDirection.OUT);
        o.setOrderDate(req.orderDate() != null ? req.orderDate() : LocalDateTime.now());
        o.setExpectedDate(req.expectedDate());
        o.setTotalAmount(req.totalAmount() != null ? req.totalAmount() : BigDecimal.ZERO);
        o.setNotes(req.notes());
        // Audit timestamp: captures last modification time.
        o.setUpdatedAt(LocalDateTime.now());
        return o;
    }

    public void update(Order entity, OrderUpdateRequest req, Customer customer) {
        // Selective update: only non-null fields are applied (partial update support).
        if (req.customerId()   != null) entity.setCustomer(customer);
        if (req.orderNumber()  != null) entity.setOrderNumber(req.orderNumber());
        if (req.status()       != null) entity.setStatus(req.status());
        if (req.expectedDate() != null) entity.setExpectedDate(req.expectedDate());
        if (req.totalAmount()  != null) entity.setTotalAmount(req.totalAmount());
        if (req.notes()        != null) entity.setNotes(req.notes());
        // Update modification timestamp.
        entity.setUpdatedAt(LocalDateTime.now());
    }

    public OrderResponse toResponse(Order e) {
        return new OrderResponse(
                e.getId(), e.getCreatedAt(),
                e.getCustomer() != null ? e.getCustomer().getId() : null,
                e.getOrderNumber(), e.getStatus(), e.getDirection(),
                e.getOrderDate(), e.getExpectedDate(),
                e.getConfirmedAt(), e.getCancelledAt(),
                e.getTotalAmount(), e.getNotes()
        );
    }
}

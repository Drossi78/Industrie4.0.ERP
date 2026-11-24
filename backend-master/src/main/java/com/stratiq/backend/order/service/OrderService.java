package com.stratiq.backend.order.service;

import com.stratiq.backend.order.dto.request.OrderCreateRequest;
import com.stratiq.backend.order.dto.request.OrderUpdateRequest;
import com.stratiq.backend.order.dto.response.OrderResponse;
import com.stratiq.backend.order.model.OrderDirection;
import com.stratiq.backend.order.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderResponse create(OrderCreateRequest req);
    OrderResponse update(UUID id, OrderUpdateRequest req);
    OrderResponse get(UUID id);
    Page<OrderResponse> list(Pageable pageable);
    List<OrderResponse> findByCustomer(UUID customerId);
    List<OrderResponse> findByStatus(OrderStatus status);
    List<OrderResponse> findByDirection(OrderDirection direction);
    void delete(UUID id);

    // Confirms the order and reserves BOM components for any OWN_PRODUCT line, fullest-warehouse-first.
    // Throws InsufficientStockException (aborting the whole confirm) if any component can't be fully covered.
    OrderResponse confirm(UUID id);

    // Cancels the order and releases any ACTIVE reservations made for it.
    OrderResponse cancel(UUID id);
}

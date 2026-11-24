package com.stratiq.backend.order.service;

import com.stratiq.backend.order.dto.request.OrderItemCreateRequest;
import com.stratiq.backend.order.dto.request.OrderItemUpdateRequest;
import com.stratiq.backend.order.dto.response.OrderItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface OrderItemService {
    OrderItemResponse create(OrderItemCreateRequest req);
    OrderItemResponse update(UUID id, OrderItemUpdateRequest req);
    OrderItemResponse get(UUID id);
    Page<OrderItemResponse> list(Pageable pageable);
    List<OrderItemResponse> findByOrder(UUID orderId);
    void delete(UUID id);
}

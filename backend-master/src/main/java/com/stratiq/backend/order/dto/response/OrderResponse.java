package com.stratiq.backend.order.dto.response;

import com.stratiq.backend.order.model.OrderDirection;
import com.stratiq.backend.order.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        Instant createdAt,
        UUID customerId,
        String orderNumber,
        OrderStatus status,
        OrderDirection direction,
        LocalDateTime orderDate,
        LocalDateTime expectedDate,
        LocalDateTime confirmedAt,
        LocalDateTime cancelledAt,
        BigDecimal totalAmount,
        String notes
) {}

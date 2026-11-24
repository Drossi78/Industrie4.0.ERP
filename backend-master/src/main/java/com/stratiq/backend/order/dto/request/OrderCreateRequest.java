package com.stratiq.backend.order.dto.request;

import com.stratiq.backend.order.model.OrderDirection;
import com.stratiq.backend.order.model.OrderStatus;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderCreateRequest(
        UUID customerId,
        @Size(max = 50) String orderNumber,
        OrderStatus status,
        OrderDirection direction,
        LocalDateTime orderDate,
        LocalDateTime expectedDate,
        BigDecimal totalAmount,
        String notes
) {}

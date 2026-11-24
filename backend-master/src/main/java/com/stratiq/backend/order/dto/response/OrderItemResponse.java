package com.stratiq.backend.order.dto.response;

import com.stratiq.backend.shared.model.Unit;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import com.stratiq.backend.shared.model.Unit;

public record OrderItemResponse(
        UUID id,
        LocalDateTime createdAt,
        UUID orderId,
        UUID itemId,
        String itemName,
        Unit unit,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal,
        String notes,
        UUID supplierId
) {}

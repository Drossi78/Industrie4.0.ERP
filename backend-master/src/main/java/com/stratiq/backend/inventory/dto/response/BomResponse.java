package com.stratiq.backend.inventory.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BomResponse(
        UUID id,
        LocalDateTime createdAt,
        UUID itemId,
        String itemName,
        UUID componentId,
        String componentName,
        BigDecimal quantity,
        String unit,
        String notes
) {}

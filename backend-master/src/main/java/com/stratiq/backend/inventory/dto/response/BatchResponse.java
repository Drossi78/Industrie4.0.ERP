package com.stratiq.backend.inventory.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BatchResponse(
        UUID id,
        LocalDateTime createdAt,
        String batchNumber,
        UUID itemId,
        String itemName,
        UUID warehouseId,
        String warehouseName,
        LocalDateTime expiryDate,
        BigDecimal currentStock
) {}

package com.stratiq.backend.warehouse.dto.response;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record StockMovementResponse(
        UUID id,
        Instant createdAt,
        String note,
        Long quantity,
        String type,
        UUID itemId,
        UUID orderId,
        UUID warehouseId,
        UUID batchId,
        // Human-readable batch info (null for untracked movements), for display/traceability.
        String batchNumber,
        LocalDateTime expiryDate
) {}

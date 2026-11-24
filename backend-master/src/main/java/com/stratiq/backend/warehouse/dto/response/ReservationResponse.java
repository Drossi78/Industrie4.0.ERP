package com.stratiq.backend.warehouse.dto.response;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationResponse(
        UUID id,
        Instant createdAt,
        UUID itemId,
        String itemName,
        UUID warehouseId,
        String warehouseName,
        Long quantity,
        String status,
        String note,
        UUID orderId,
        LocalDateTime releasedAt
) {}

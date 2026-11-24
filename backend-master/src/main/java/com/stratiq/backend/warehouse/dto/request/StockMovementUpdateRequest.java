package com.stratiq.backend.warehouse.dto.request;

import jakarta.validation.constraints.Size;

import java.util.UUID;

public record StockMovementUpdateRequest(
        @Size(max = 255) String note,
        Long quantity,
        @Size(max = 255) String type,
        UUID itemId,
        UUID orderId,
        UUID warehouseId
) {}

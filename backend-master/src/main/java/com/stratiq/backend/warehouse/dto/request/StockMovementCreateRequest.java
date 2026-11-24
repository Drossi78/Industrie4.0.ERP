package com.stratiq.backend.warehouse.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record StockMovementCreateRequest(
        @Size(max = 255) String note,
        @NotNull Long quantity,
        @NotNull @Size(max = 255) String type,
        @NotNull UUID itemId,
        UUID orderId,
        @NotNull UUID warehouseId
) {}

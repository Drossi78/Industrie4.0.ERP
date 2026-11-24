package com.stratiq.backend.warehouse.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

// Stock adjustment: corrects on-hand quantity up or down (quantity may be
// negative or positive, but the target item, warehouse and amount are required).
public record AdjustmentRequest(
        @NotNull UUID itemId,
        @NotNull UUID warehouseId,
        @NotNull Long quantity,
        @Size(max = 255) String note
) {}

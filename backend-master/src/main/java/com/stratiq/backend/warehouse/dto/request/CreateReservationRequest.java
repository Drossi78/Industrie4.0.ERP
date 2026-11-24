package com.stratiq.backend.warehouse.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.UUID;

// Reserve a quantity of an item in a warehouse. Fails if available stock is insufficient.
public record CreateReservationRequest(
        @NotNull UUID itemId,
        @NotNull UUID warehouseId,
        @NotNull @Positive Long quantity,
        @Size(max = 255) String note,
        UUID orderId
) {}

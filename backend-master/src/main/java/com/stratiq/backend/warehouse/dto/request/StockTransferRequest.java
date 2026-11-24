package com.stratiq.backend.warehouse.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.UUID;

// Umlagerung: moves stock of an item from one warehouse to another.
// Booked as an OUT in the source and an IN in the target warehouse.
public record StockTransferRequest(
        @NotNull UUID itemId,
        @NotNull UUID fromWarehouseId,
        @NotNull UUID toWarehouseId,
        @NotNull @Positive Long quantity,
        @Size(max = 255) String note
) {}

package com.stratiq.backend.warehouse.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

// Wareneingang: books a positive IN movement for an item into a warehouse.
// If batchNumber is set, the movement is booked onto that lot (created if new);
// leave it null for untracked stock. expiryDate is only applied when a new batch is created.
public record GoodsReceiptRequest(
        @NotNull UUID itemId,
        @NotNull UUID warehouseId,
        @NotNull @Positive Long quantity,
        @Size(max = 100) String batchNumber,
        LocalDateTime expiryDate,
        @Size(max = 255) String note,
        UUID orderId
) {}

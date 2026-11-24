package com.stratiq.backend.warehouse.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.UUID;

// Warenausgang: books positive OUT movement(s); fails if available stock is insufficient.
// If batchId is set, stock is taken from that specific lot (manual selection). Otherwise it is
// allocated automatically across batches FEFO-then-FIFO, possibly spanning several lots.
public record GoodsIssueRequest(
        @NotNull UUID itemId,
        @NotNull UUID warehouseId,
        @NotNull @Positive Long quantity,
        UUID batchId,
        @Size(max = 255) String note,
        UUID orderId
) {}

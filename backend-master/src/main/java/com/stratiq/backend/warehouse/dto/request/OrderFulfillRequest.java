package com.stratiq.backend.warehouse.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// Book stock movements for all items of an order.
// direction: "IN" (goods receipt, requires warehouseId) or "OUT" (goods issue,
// auto-allocated across warehouses; warehouseId optional/ignored).
// batchLines: optional per-item batch metadata, only applied for direction IN.
public record OrderFulfillRequest(
        UUID warehouseId,
        @NotNull String direction,
        List<BatchLine> batchLines
) {
    // Batch info for one order item's goods receipt. batchNumber blank/null = untracked.
    public record BatchLine(
            UUID itemId,
            String batchNumber,
            LocalDateTime expiryDate
    ) {}
}

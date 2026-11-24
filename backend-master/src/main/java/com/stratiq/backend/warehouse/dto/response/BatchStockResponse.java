package com.stratiq.backend.warehouse.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

// Derived available stock of a single batch (SUM(IN) - SUM(OUT)) for an item in a warehouse.
// Used to populate the batch picker for a manual goods issue.
public record BatchStockResponse(
        UUID batchId,
        String batchNumber,
        LocalDateTime expiryDate,
        Long balance
) {}

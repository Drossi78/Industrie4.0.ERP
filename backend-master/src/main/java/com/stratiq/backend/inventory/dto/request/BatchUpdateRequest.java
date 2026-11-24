package com.stratiq.backend.inventory.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;

// A batch is metadata only. Stock is adjusted via stock movements, not here.
public record BatchUpdateRequest(
        String batchNumber,
        UUID itemId,
        UUID warehouseId,
        LocalDateTime expiryDate
) {}

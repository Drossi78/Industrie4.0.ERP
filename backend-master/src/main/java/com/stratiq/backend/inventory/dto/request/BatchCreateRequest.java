package com.stratiq.backend.inventory.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;

// A batch is metadata only. Stock enters via a goods receipt (IN movement), not here.
public record BatchCreateRequest(
        String batchNumber,
        UUID itemId,
        UUID warehouseId,
        LocalDateTime expiryDate
) {}

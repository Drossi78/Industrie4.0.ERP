package com.stratiq.backend.inventory.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

public record BomCreateRequest(
        UUID itemId,
        UUID componentId,
        BigDecimal quantity,
        String unit,
        String notes
) {}

package com.stratiq.backend.inventory.dto.request;

import com.stratiq.backend.inventory.model.ItemKind;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemUpdateRequest(
        String name,
        ItemKind kind,
        UUID categoryId,
        String unit,
        BigDecimal price,
        String description,
        String notes
) {}

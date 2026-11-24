package com.stratiq.backend.inventory.dto.response;

import com.stratiq.backend.inventory.model.ItemKind;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ItemResponse(
        UUID id,
        LocalDateTime createdAt,
        String name,
        ItemKind kind,
        UUID categoryId,
        String categoryName,
        String unit,
        BigDecimal price,
        String description,
        String notes,
        boolean isDeleted
) {}

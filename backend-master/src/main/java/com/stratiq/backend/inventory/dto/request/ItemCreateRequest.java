package com.stratiq.backend.inventory.dto.request;

import com.stratiq.backend.inventory.model.ItemKind;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemCreateRequest(
        @NotBlank String name,
        ItemKind kind,
        UUID categoryId,
        String unit,
        BigDecimal price,
        String description,
        String notes
) {}

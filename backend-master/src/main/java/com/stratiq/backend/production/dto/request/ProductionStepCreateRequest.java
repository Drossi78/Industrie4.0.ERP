package com.stratiq.backend.production.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ProductionStepCreateRequest(
        @NotNull UUID itemId,
        @NotBlank @Size(max = 255) String name,
        String description,
        @NotNull @Min(1) int stepOrder,
        String notes
) {}

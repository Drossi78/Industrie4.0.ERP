package com.stratiq.backend.production.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record ProductionStepUpdateRequest(
        @Size(max = 255) String name,
        String description,
        @Min(1) Integer stepOrder,
        String notes,
        Boolean isDeleted
) {}

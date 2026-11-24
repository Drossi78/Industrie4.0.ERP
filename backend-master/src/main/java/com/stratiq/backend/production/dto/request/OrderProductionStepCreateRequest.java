package com.stratiq.backend.production.dto.request;

import com.stratiq.backend.production.model.ProductionStepStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record OrderProductionStepCreateRequest(
        @NotNull UUID orderId,
        @NotNull UUID orderItemId,
        @NotNull UUID templateStepId,
        @NotBlank @Size(max = 255) String name,
        String description,
        @NotNull @Min(1) int stepOrder,
        ProductionStepStatus status,
        UUID assignedTo
) {}

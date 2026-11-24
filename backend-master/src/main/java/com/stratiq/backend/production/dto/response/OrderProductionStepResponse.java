package com.stratiq.backend.production.dto.response;

import com.stratiq.backend.production.model.ProductionStepStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderProductionStepResponse(
        UUID id,
        LocalDateTime createdAt,
        UUID orderId,
        UUID orderItemId,
        UUID templateStepId,
        String name,
        String description,
        int stepOrder,
        ProductionStepStatus status,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        UUID assignedTo
) {}

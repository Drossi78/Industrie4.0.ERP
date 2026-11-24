package com.stratiq.backend.production.dto.request;

import com.stratiq.backend.production.model.ProductionStepStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderProductionStepUpdateRequest(
        ProductionStepStatus status,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        UUID assignedTo,
        String description
) {}

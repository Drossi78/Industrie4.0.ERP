package com.stratiq.backend.production.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProductionStepResponse(
        UUID id,
        LocalDateTime createdAt,
        UUID itemId,
        String name,
        String description,
        int stepOrder,
        boolean isDeleted,
        String notes
) {}

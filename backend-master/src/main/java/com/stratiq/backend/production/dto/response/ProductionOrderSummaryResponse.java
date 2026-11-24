package com.stratiq.backend.production.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProductionOrderSummaryResponse(
        UUID id,
        String orderNumber,
        String customerName,
        LocalDateTime orderDate,
        int totalSteps,
        int completedSteps,
        int progressPercent
) {}

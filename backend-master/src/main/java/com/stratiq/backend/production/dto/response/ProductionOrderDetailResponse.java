package com.stratiq.backend.production.dto.response;

import com.stratiq.backend.production.model.ProductionStepStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ProductionOrderDetailResponse(
        UUID id,
        String orderNumber,
        String customerName,
        List<AssetDetail> assets
) {
    public record AssetDetail(
            UUID id,
            String itemName,
            BigDecimal quantity,
            int totalSteps,
            int completedSteps,
            List<StepDetail> steps
    ) {}

    public record StepDetail(
            UUID id,
            String name,
            int stepOrder,
            ProductionStepStatus status,
            AssignedUserInfo assignedTo,
            LocalDateTime startedAt,
            LocalDateTime completedAt
    ) {}

    public record AssignedUserInfo(UUID id, String email) {}
}

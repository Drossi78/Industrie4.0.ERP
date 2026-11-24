package com.stratiq.backend.production.repository;

import com.stratiq.backend.production.model.OrderProductionStep;
import com.stratiq.backend.production.model.ProductionStepStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderProductionStepRepository extends JpaRepository<OrderProductionStep, UUID> {
    // Get all production steps for an order, ordered by execution sequence.
    List<OrderProductionStep> findByOrderIdOrderByStepOrderAsc(UUID orderId);
    // Get production steps for a specific order item, ordered by sequence (granular line-item tracking).
    List<OrderProductionStep> findByOrderItemIdOrderByStepOrderAsc(UUID orderItemId);
    // Filter by step status (PENDING, IN_PROGRESS, COMPLETED, FAILED) for workflow tracking.
    List<OrderProductionStep> findByStatus(ProductionStepStatus status);
    // Find all production steps assigned to a specific user (workload assignment).
    List<OrderProductionStep> findByAssignedTo(UUID userId);
}

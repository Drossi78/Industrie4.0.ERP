package com.stratiq.backend.production.mapper;

import com.stratiq.backend.production.dto.request.OrderProductionStepCreateRequest;
import com.stratiq.backend.production.dto.request.OrderProductionStepUpdateRequest;
import com.stratiq.backend.production.dto.response.OrderProductionStepResponse;
import com.stratiq.backend.order.model.Order;
import com.stratiq.backend.order.model.OrderItem;
import com.stratiq.backend.production.model.OrderProductionStep;
import com.stratiq.backend.production.model.ProductionStep;
import com.stratiq.backend.production.model.ProductionStepStatus;
import org.springframework.stereotype.Component;
import com.stratiq.backend.order.model.Order;
import com.stratiq.backend.order.model.OrderItem;

@Component
public class OrderProductionStepMapper {

    public OrderProductionStep toEntity(OrderProductionStepCreateRequest req,
                                         Order order,
                                         OrderItem orderItem,
                                         ProductionStep templateStep) {
        // Map to entity; bridges Order domain (required), OrderItem (optional), and Production domain (templateStep).
        OrderProductionStep ops = new OrderProductionStep();
        ops.setOrder(order);
        ops.setOrderItem(orderItem);
        ops.setTemplateStep(templateStep);
        ops.setName(req.name());
        ops.setDescription(req.description());
        // stepOrder preserves workflow sequence for the specific order.
        ops.setStepOrder(req.stepOrder());
        // Default status: PENDING (not yet started).
        ops.setStatus(req.status() != null ? req.status() : ProductionStepStatus.PENDING);
        ops.setAssignedTo(req.assignedTo());
        return ops;
    }

    public void update(OrderProductionStep entity, OrderProductionStepUpdateRequest req) {
        // Selective update: null fields skipped (supports status transitions, time tracking, reassignments).
        if (req.status()      != null) entity.setStatus(req.status());
        if (req.startedAt()   != null) entity.setStartedAt(req.startedAt());
        if (req.completedAt() != null) entity.setCompletedAt(req.completedAt());
        if (req.assignedTo()  != null) entity.setAssignedTo(req.assignedTo());
        if (req.description() != null) entity.setDescription(req.description());
    }

    public OrderProductionStepResponse toResponse(OrderProductionStep e) {
        return new OrderProductionStepResponse(
                e.getId(), e.getCreatedAt(),
                e.getOrder().getId(),
                e.getOrderItem().getId(),
                e.getTemplateStep().getId(),
                e.getName(), e.getDescription(),
                e.getStepOrder(), e.getStatus(),
                e.getStartedAt(), e.getCompletedAt(),
                e.getAssignedTo()
        );
    }
}

package com.stratiq.backend.production.mapper;

import com.stratiq.backend.order.model.Order;
import com.stratiq.backend.order.model.OrderItem;
import com.stratiq.backend.production.dto.request.OrderProductionStepCreateRequest;
import com.stratiq.backend.production.dto.request.OrderProductionStepUpdateRequest;
import com.stratiq.backend.production.dto.response.OrderProductionStepResponse;
import com.stratiq.backend.production.model.OrderProductionStep;
import com.stratiq.backend.production.model.ProductionStep;
import com.stratiq.backend.production.model.ProductionStepStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderProductionStepMapperTest {

    private final OrderProductionStepMapper mapper = new OrderProductionStepMapper();

    @Test
    void toEntity_withExplicitStatus_usesProvidedStatus() {
        Order order = new Order();
        order.setId(UUID.randomUUID());
        OrderItem orderItem = new OrderItem();
        orderItem.setId(UUID.randomUUID());
        ProductionStep templateStep = new ProductionStep();
        templateStep.setId(UUID.randomUUID());

        OrderProductionStepCreateRequest req = new OrderProductionStepCreateRequest(
                order.getId(), orderItem.getId(), templateStep.getId(),
                "Assembly", "Assemble parts", 2, ProductionStepStatus.IN_PROGRESS, null
        );

        OrderProductionStep result = mapper.toEntity(req, order, orderItem, templateStep);

        assertThat(result.getOrder()).isEqualTo(order);
        assertThat(result.getOrderItem()).isEqualTo(orderItem);
        assertThat(result.getTemplateStep()).isEqualTo(templateStep);
        assertThat(result.getName()).isEqualTo("Assembly");
        assertThat(result.getStepOrder()).isEqualTo(2);
        assertThat(result.getStatus()).isEqualTo(ProductionStepStatus.IN_PROGRESS);
    }

    @Test
    void toEntity_withNullStatus_defaultsToPending() {
        OrderProductionStepCreateRequest req = new OrderProductionStepCreateRequest(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "Step", null, 1, null, null
        );

        OrderProductionStep result = mapper.toEntity(req, new Order(), new OrderItem(), new ProductionStep());

        assertThat(result.getStatus()).isEqualTo(ProductionStepStatus.PENDING);
    }

    @Test
    void update_withNonNullFields_overwritesExisting() {
        OrderProductionStep step = new OrderProductionStep();
        step.setStatus(ProductionStepStatus.PENDING);
        LocalDateTime started = LocalDateTime.now();
        OrderProductionStepUpdateRequest req = new OrderProductionStepUpdateRequest(
                ProductionStepStatus.IN_PROGRESS, started, null, null, "Updated desc"
        );

        mapper.update(step, req);

        assertThat(step.getStatus()).isEqualTo(ProductionStepStatus.IN_PROGRESS);
        assertThat(step.getStartedAt()).isEqualTo(started);
        assertThat(step.getDescription()).isEqualTo("Updated desc");
    }

    @Test
    void update_withNullFields_keepsExistingValues() {
        OrderProductionStep step = new OrderProductionStep();
        step.setStatus(ProductionStepStatus.PENDING);
        step.setDescription("Keep me");
        OrderProductionStepUpdateRequest req = new OrderProductionStepUpdateRequest(
                null, null, null, null, null
        );

        mapper.update(step, req);

        assertThat(step.getStatus()).isEqualTo(ProductionStepStatus.PENDING);
        assertThat(step.getDescription()).isEqualTo("Keep me");
    }

    @Test
    void toResponse_mapsRelatedEntityIds() {
        Order order = new Order();
        order.setId(UUID.randomUUID());
        OrderItem orderItem = new OrderItem();
        orderItem.setId(UUID.randomUUID());
        ProductionStep templateStep = new ProductionStep();
        templateStep.setId(UUID.randomUUID());

        OrderProductionStep step = new OrderProductionStep();
        step.setId(UUID.randomUUID());
        step.setOrder(order);
        step.setOrderItem(orderItem);
        step.setTemplateStep(templateStep);
        step.setName("Assembly");
        step.setStepOrder(1);
        step.setStatus(ProductionStepStatus.PENDING);

        OrderProductionStepResponse response = mapper.toResponse(step);

        assertThat(response.orderId()).isEqualTo(order.getId());
        assertThat(response.orderItemId()).isEqualTo(orderItem.getId());
        assertThat(response.templateStepId()).isEqualTo(templateStep.getId());
        assertThat(response.status()).isEqualTo(ProductionStepStatus.PENDING);
    }
}

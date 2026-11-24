package com.stratiq.backend.production.mapper;

import com.stratiq.backend.inventory.model.Item;
import com.stratiq.backend.production.dto.request.ProductionStepCreateRequest;
import com.stratiq.backend.production.dto.request.ProductionStepUpdateRequest;
import com.stratiq.backend.production.dto.response.ProductionStepResponse;
import com.stratiq.backend.production.model.ProductionStep;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProductionStepMapperTest {

    private final ProductionStepMapper mapper = new ProductionStepMapper();

    @Test
    void toEntity_mapsAllFields() {
        Item item = new Item();
        item.setId(UUID.randomUUID());
        ProductionStepCreateRequest req = new ProductionStepCreateRequest(
                item.getId(), "Cutting", "Cut the material", 1, "Use saw"
        );

        ProductionStep result = mapper.toEntity(req, item);

        assertThat(result.getItem()).isEqualTo(item);
        assertThat(result.getName()).isEqualTo("Cutting");
        assertThat(result.getDescription()).isEqualTo("Cut the material");
        assertThat(result.getStepOrder()).isEqualTo(1);
        assertThat(result.getNotes()).isEqualTo("Use saw");
    }

    @Test
    void update_withNonNullFields_overwritesExisting() {
        ProductionStep step = new ProductionStep();
        step.setName("Old Name");
        step.setStepOrder(1);
        ProductionStepUpdateRequest req = new ProductionStepUpdateRequest(
                "New Name", null, 2, null, null
        );

        mapper.update(step, req);

        assertThat(step.getName()).isEqualTo("New Name");
        assertThat(step.getStepOrder()).isEqualTo(2);
    }

    @Test
    void update_withNullFields_keepsExistingValues() {
        ProductionStep step = new ProductionStep();
        step.setName("Keep Me");
        step.setStepOrder(3);
        ProductionStepUpdateRequest req = new ProductionStepUpdateRequest(
                null, null, null, null, null
        );

        mapper.update(step, req);

        assertThat(step.getName()).isEqualTo("Keep Me");
        assertThat(step.getStepOrder()).isEqualTo(3);
    }

    @Test
    void update_withIsDeleted_setsDeletedFlag() {
        ProductionStep step = new ProductionStep();
        ProductionStepUpdateRequest req = new ProductionStepUpdateRequest(
                null, null, null, null, true
        );

        mapper.update(step, req);

        assertThat(step.isDeleted()).isTrue();
    }

    @Test
    void toResponse_mapsItemId() {
        Item item = new Item();
        UUID itemId = UUID.randomUUID();
        item.setId(itemId);

        ProductionStep step = new ProductionStep();
        step.setId(UUID.randomUUID());
        step.setItem(item);
        step.setName("Cutting");
        step.setStepOrder(1);

        ProductionStepResponse response = mapper.toResponse(step);

        assertThat(response.itemId()).isEqualTo(itemId);
        assertThat(response.name()).isEqualTo("Cutting");
        assertThat(response.stepOrder()).isEqualTo(1);
    }
}

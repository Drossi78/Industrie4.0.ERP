package com.stratiq.backend.warehouse.mapper;

import com.stratiq.backend.warehouse.dto.request.WarehouseCreateRequest;
import com.stratiq.backend.warehouse.dto.request.WarehouseUpdateRequest;
import com.stratiq.backend.warehouse.dto.response.WarehouseResponse;
import com.stratiq.backend.warehouse.model.Warehouse;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WarehouseMapperTest {

    private final WarehouseMapper mapper = new WarehouseMapper();

    @Test
    void toEntity_mapsAllFields() {
        WarehouseCreateRequest req = new WarehouseCreateRequest(
                "Main Warehouse", "Primary storage", "Warehouse Ave", "10",
                "10115", "Berlin", "BE", "DE", true
        );

        Warehouse result = mapper.toEntity(req);

        assertThat(result.getName()).isEqualTo("Main Warehouse");
        assertThat(result.getDescription()).isEqualTo("Primary storage");
        assertThat(result.getCity()).isEqualTo("Berlin");
        assertThat(result.getCountry()).isEqualTo("DE");
        assertThat(result.isActive()).isTrue();
        assertThat(result.getUpdatedAt()).isNotNull();
    }

    @Test
    void toEntity_withNullIsActive_defaultsToTrue() {
        WarehouseCreateRequest req = new WarehouseCreateRequest(
                "Warehouse", null, null, null, null, null, null, null, null
        );

        Warehouse result = mapper.toEntity(req);

        assertThat(result.isActive()).isTrue();
    }

    @Test
    void toEntity_withExplicitFalseIsActive_setsFalse() {
        WarehouseCreateRequest req = new WarehouseCreateRequest(
                "Warehouse", null, null, null, null, null, null, null, false
        );

        Warehouse result = mapper.toEntity(req);

        assertThat(result.isActive()).isFalse();
    }

    @Test
    void update_withNonNullFields_overwritesExisting() {
        Warehouse warehouse = new Warehouse();
        warehouse.setName("Old Name");
        warehouse.setCity("Hamburg");
        warehouse.setActive(true);

        WarehouseUpdateRequest req = new WarehouseUpdateRequest(
                "New Name", null, null, null, null, "Munich", null, null, false
        );

        mapper.update(warehouse, req);

        assertThat(warehouse.getName()).isEqualTo("New Name");
        assertThat(warehouse.getCity()).isEqualTo("Munich");
        assertThat(warehouse.isActive()).isFalse();
        assertThat(warehouse.getUpdatedAt()).isNotNull();
    }

    @Test
    void update_withNullFields_keepsExistingValues() {
        Warehouse warehouse = new Warehouse();
        warehouse.setName("Keep Me");
        warehouse.setCity("Berlin");
        warehouse.setActive(true);

        WarehouseUpdateRequest req = new WarehouseUpdateRequest(
                null, null, null, null, null, null, null, null, null
        );

        mapper.update(warehouse, req);

        assertThat(warehouse.getName()).isEqualTo("Keep Me");
        assertThat(warehouse.getCity()).isEqualTo("Berlin");
        assertThat(warehouse.isActive()).isTrue();
    }

    @Test
    void toResponse_mapsAllFields() {
        Warehouse w = new Warehouse();
        w.setId(UUID.randomUUID());
        w.setName("Main Warehouse");
        w.setCity("Berlin");
        w.setActive(true);

        WarehouseResponse response = mapper.toResponse(w);

        assertThat(response.id()).isEqualTo(w.getId());
        assertThat(response.name()).isEqualTo("Main Warehouse");
        assertThat(response.city()).isEqualTo("Berlin");
        assertThat(response.isActive()).isTrue();
    }
}

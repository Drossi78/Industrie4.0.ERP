package com.stratiq.backend.inventory.mapper;

import com.stratiq.backend.inventory.dto.response.BatchResponse;
import com.stratiq.backend.inventory.model.Batch;
import com.stratiq.backend.inventory.model.Item;
import com.stratiq.backend.warehouse.model.Warehouse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BatchMapperTest {

    private final BatchMapper mapper = new BatchMapper();

    @Test
    void toResponse_withItemAndWarehouse_includesNamesAndIds() {
        Item item = new Item();
        item.setId(UUID.randomUUID());
        item.setName("Widget");

        Warehouse warehouse = new Warehouse();
        warehouse.setId(UUID.randomUUID());
        warehouse.setName("Main Warehouse");

        Batch batch = new Batch();
        batch.setId(UUID.randomUUID());
        batch.setBatchNumber("B-001");
        batch.setItem(item);
        batch.setWarehouse(warehouse);

        // currentStock is derived and supplied by the service, no longer stored on the entity.
        BatchResponse response = mapper.toResponse(batch, new BigDecimal("50.000"));

        assertThat(response.batchNumber()).isEqualTo("B-001");
        assertThat(response.itemId()).isEqualTo(item.getId());
        assertThat(response.itemName()).isEqualTo("Widget");
        assertThat(response.warehouseId()).isEqualTo(warehouse.getId());
        assertThat(response.warehouseName()).isEqualTo("Main Warehouse");
        assertThat(response.currentStock()).isEqualByComparingTo("50.000");
    }

    @Test
    void toResponse_withNullItemAndWarehouse_returnsNullIds() {
        Batch batch = new Batch();
        batch.setId(UUID.randomUUID());
        batch.setBatchNumber("B-002");

        BatchResponse response = mapper.toResponse(batch, BigDecimal.ZERO);

        assertThat(response.itemId()).isNull();
        assertThat(response.itemName()).isNull();
        assertThat(response.warehouseId()).isNull();
        assertThat(response.warehouseName()).isNull();
    }
}

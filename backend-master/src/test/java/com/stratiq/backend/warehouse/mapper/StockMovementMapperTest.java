package com.stratiq.backend.warehouse.mapper;

import com.stratiq.backend.inventory.model.Item;
import com.stratiq.backend.order.model.Order;
import com.stratiq.backend.warehouse.dto.request.StockMovementCreateRequest;
import com.stratiq.backend.warehouse.dto.request.StockMovementUpdateRequest;
import com.stratiq.backend.warehouse.dto.response.StockMovementResponse;
import com.stratiq.backend.warehouse.model.StockMovement;
import com.stratiq.backend.warehouse.model.Warehouse;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class StockMovementMapperTest {

    private final StockMovementMapper mapper = new StockMovementMapper();

    @Test
    void toEntity_mapsAllFields() {
        Item item = new Item();
        item.setId(UUID.randomUUID());
        Order order = new Order();
        order.setId(UUID.randomUUID());
        Warehouse warehouse = new Warehouse();
        warehouse.setId(UUID.randomUUID());

        StockMovementCreateRequest req = new StockMovementCreateRequest(
                "Incoming shipment", 100L, "IN",
                item.getId(), order.getId(), warehouse.getId()
        );

        StockMovement result = mapper.toEntity(req, item, order, warehouse);

        assertThat(result.getNote()).isEqualTo("Incoming shipment");
        assertThat(result.getQuantity()).isEqualTo(100L);
        assertThat(result.getType()).isEqualTo("IN");
        assertThat(result.getItem()).isEqualTo(item);
        assertThat(result.getOrder()).isEqualTo(order);
        assertThat(result.getWarehouse()).isEqualTo(warehouse);
    }

    @Test
    void update_withNonNullFields_overwritesExisting() {
        Item newItem = new Item();
        newItem.setId(UUID.randomUUID());
        Warehouse newWarehouse = new Warehouse();
        newWarehouse.setId(UUID.randomUUID());

        StockMovement movement = new StockMovement();
        movement.setQuantity(50L);
        movement.setType("IN");

        StockMovementUpdateRequest req = new StockMovementUpdateRequest(
                "Updated note", 200L, "OUT", newItem.getId(), null, newWarehouse.getId()
        );

        mapper.update(movement, req, newItem, null, newWarehouse);

        assertThat(movement.getNote()).isEqualTo("Updated note");
        assertThat(movement.getQuantity()).isEqualTo(200L);
        assertThat(movement.getType()).isEqualTo("OUT");
        assertThat(movement.getItem()).isEqualTo(newItem);
        assertThat(movement.getWarehouse()).isEqualTo(newWarehouse);
    }

    @Test
    void update_withNullFields_keepsExistingValues() {
        StockMovement movement = new StockMovement();
        movement.setQuantity(50L);
        movement.setType("IN");

        StockMovementUpdateRequest req = new StockMovementUpdateRequest(
                null, null, null, null, null, null
        );

        mapper.update(movement, req, null, null, null);

        assertThat(movement.getQuantity()).isEqualTo(50L);
        assertThat(movement.getType()).isEqualTo("IN");
    }

    @Test
    void toResponse_withAllRelations_includesIds() {
        Item item = new Item();
        item.setId(UUID.randomUUID());
        Order order = new Order();
        order.setId(UUID.randomUUID());
        Warehouse warehouse = new Warehouse();
        warehouse.setId(UUID.randomUUID());

        StockMovement movement = new StockMovement();
        movement.setId(UUID.randomUUID());
        movement.setQuantity(10L);
        movement.setType("IN");
        movement.setItem(item);
        movement.setOrder(order);
        movement.setWarehouse(warehouse);

        StockMovementResponse response = mapper.toResponse(movement);

        assertThat(response.itemId()).isEqualTo(item.getId());
        assertThat(response.orderId()).isEqualTo(order.getId());
        assertThat(response.warehouseId()).isEqualTo(warehouse.getId());
        assertThat(response.quantity()).isEqualTo(10L);
        assertThat(response.type()).isEqualTo("IN");
    }

    @Test
    void toResponse_withNullRelations_returnsNullIds() {
        StockMovement movement = new StockMovement();
        movement.setId(UUID.randomUUID());
        movement.setQuantity(5L);
        movement.setType("OUT");

        StockMovementResponse response = mapper.toResponse(movement);

        assertThat(response.itemId()).isNull();
        assertThat(response.orderId()).isNull();
        assertThat(response.warehouseId()).isNull();
    }
}

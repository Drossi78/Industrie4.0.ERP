package com.stratiq.backend.order.mapper;

import com.stratiq.backend.inventory.model.Item;
import com.stratiq.backend.inventory.model.Supplier;
import com.stratiq.backend.order.dto.request.OrderItemCreateRequest;
import com.stratiq.backend.order.dto.request.OrderItemUpdateRequest;
import com.stratiq.backend.order.dto.response.OrderItemResponse;
import com.stratiq.backend.order.model.Order;
import com.stratiq.backend.order.model.OrderItem;
import com.stratiq.backend.shared.model.Unit;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderItemMapperTest {

    private final OrderItemMapper mapper = new OrderItemMapper();

    @Test
    void toEntity_mapsAllFields() {
        Order order = new Order();
        order.setId(UUID.randomUUID());
        Item item = new Item();
        item.setId(UUID.randomUUID());
        Supplier supplier = new Supplier();
        supplier.setId(UUID.randomUUID());

        OrderItemCreateRequest req = new OrderItemCreateRequest(
                order.getId(), item.getId(), "Widget", Unit.piece,
                new BigDecimal("10.000"), new BigDecimal("5.00"), "Rush order", supplier.getId()
        );

        OrderItem result = mapper.toEntity(req, order, item, supplier);

        assertThat(result.getOrder()).isEqualTo(order);
        assertThat(result.getItem()).isEqualTo(item);
        assertThat(result.getItemName()).isEqualTo("Widget");
        assertThat(result.getUnit()).isEqualTo(Unit.piece);
        assertThat(result.getQuantity()).isEqualByComparingTo("10.000");
        assertThat(result.getUnitPrice()).isEqualByComparingTo("5.00");
        assertThat(result.getSupplier()).isEqualTo(supplier);
    }

    @Test
    void update_withNonNullFields_overwritesExisting() {
        Supplier newSupplier = new Supplier();
        newSupplier.setId(UUID.randomUUID());
        OrderItem item = new OrderItem();
        item.setItemName("Old");
        item.setUnit(Unit.kg);

        OrderItemUpdateRequest req = new OrderItemUpdateRequest(
                "New", Unit.piece, null, null, "Updated", newSupplier.getId()
        );

        mapper.update(item, req, newSupplier);

        assertThat(item.getItemName()).isEqualTo("New");
        assertThat(item.getUnit()).isEqualTo(Unit.piece);
        assertThat(item.getSupplier()).isEqualTo(newSupplier);
        assertThat(item.getNotes()).isEqualTo("Updated");
    }

    @Test
    void update_withNullFields_keepsExistingValues() {
        OrderItem item = new OrderItem();
        item.setItemName("Keep Me");
        item.setUnit(Unit.kg);

        OrderItemUpdateRequest req = new OrderItemUpdateRequest(
                null, null, null, null, null, null
        );

        mapper.update(item, req, null);

        assertThat(item.getItemName()).isEqualTo("Keep Me");
        assertThat(item.getUnit()).isEqualTo(Unit.kg);
    }

    @Test
    void toResponse_mapsOrderAndItemIds() {
        Order order = new Order();
        order.setId(UUID.randomUUID());
        Item item = new Item();
        item.setId(UUID.randomUUID());

        OrderItem orderItem = new OrderItem();
        orderItem.setId(UUID.randomUUID());
        orderItem.setOrder(order);
        orderItem.setItem(item);
        orderItem.setItemName("Widget");
        orderItem.setUnit(Unit.piece);
        orderItem.setQuantity(new BigDecimal("5.000"));
        orderItem.setUnitPrice(new BigDecimal("10.00"));
        orderItem.setLineTotal(new BigDecimal("50.00"));

        OrderItemResponse response = mapper.toResponse(orderItem);

        assertThat(response.orderId()).isEqualTo(order.getId());
        assertThat(response.itemId()).isEqualTo(item.getId());
        assertThat(response.itemName()).isEqualTo("Widget");
        assertThat(response.supplierId()).isNull();
    }
}

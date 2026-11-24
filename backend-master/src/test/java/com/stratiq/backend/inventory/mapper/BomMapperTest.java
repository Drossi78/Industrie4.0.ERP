package com.stratiq.backend.inventory.mapper;

import com.stratiq.backend.inventory.dto.response.BomResponse;
import com.stratiq.backend.inventory.model.BillOfMaterial;
import com.stratiq.backend.inventory.model.Item;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BomMapperTest {

    private final BomMapper mapper = new BomMapper();

    @Test
    void toResponse_withItemAndComponent_includesNamesAndIds() {
        Item item = new Item();
        item.setId(UUID.randomUUID());
        item.setName("Assembled Product");

        Item component = new Item();
        component.setId(UUID.randomUUID());
        component.setName("Component Part");

        BillOfMaterial bom = new BillOfMaterial();
        bom.setId(UUID.randomUUID());
        bom.setItem(item);
        bom.setComponent(component);
        bom.setQuantity(new BigDecimal("2.000"));
        bom.setUnit("piece");

        BomResponse response = mapper.toResponse(bom);

        assertThat(response.itemId()).isEqualTo(item.getId());
        assertThat(response.itemName()).isEqualTo("Assembled Product");
        assertThat(response.componentId()).isEqualTo(component.getId());
        assertThat(response.componentName()).isEqualTo("Component Part");
        assertThat(response.quantity()).isEqualByComparingTo("2.000");
        assertThat(response.unit()).isEqualTo("piece");
    }

    @Test
    void toResponse_withNullItemAndComponent_returnsNullIds() {
        BillOfMaterial bom = new BillOfMaterial();
        bom.setId(UUID.randomUUID());
        bom.setQuantity(BigDecimal.ONE);

        BomResponse response = mapper.toResponse(bom);

        assertThat(response.itemId()).isNull();
        assertThat(response.itemName()).isNull();
        assertThat(response.componentId()).isNull();
        assertThat(response.componentName()).isNull();
    }
}

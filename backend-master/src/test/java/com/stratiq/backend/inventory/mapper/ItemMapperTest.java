package com.stratiq.backend.inventory.mapper;

import com.stratiq.backend.inventory.dto.response.ItemResponse;
import com.stratiq.backend.inventory.model.Category;
import com.stratiq.backend.inventory.model.Item;
import com.stratiq.backend.inventory.model.ItemKind;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ItemMapperTest {

    private final ItemMapper mapper = new ItemMapper();

    @Test
    void toResponse_withCategory_includesCategoryDetails() {
        Category cat = new Category();
        cat.setId(UUID.randomUUID());
        cat.setName("Electronics");

        Item item = new Item();
        item.setId(UUID.randomUUID());
        item.setName("Laptop");
        item.setKind(ItemKind.OWN_PRODUCT);
        item.setCategory(cat);
        item.setUnit("piece");
        item.setPrice(new BigDecimal("999.99"));

        ItemResponse response = mapper.toResponse(item);

        assertThat(response.name()).isEqualTo("Laptop");
        assertThat(response.kind()).isEqualTo(ItemKind.OWN_PRODUCT);
        assertThat(response.categoryId()).isEqualTo(cat.getId());
        assertThat(response.categoryName()).isEqualTo("Electronics");
        assertThat(response.unit()).isEqualTo("piece");
        assertThat(response.price()).isEqualByComparingTo("999.99");
    }

    @Test
    void toResponse_withoutCategory_categoryFieldsAreNull() {
        Item item = new Item();
        item.setId(UUID.randomUUID());
        item.setName("Laptop");
        item.setKind(ItemKind.OWN_PRODUCT);

        ItemResponse response = mapper.toResponse(item);

        assertThat(response.categoryId()).isNull();
        assertThat(response.categoryName()).isNull();
    }
}

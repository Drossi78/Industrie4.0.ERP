package com.stratiq.backend.inventory.mapper;

import com.stratiq.backend.inventory.dto.request.CategoryCreateRequest;
import com.stratiq.backend.inventory.dto.response.CategoryResponse;
import com.stratiq.backend.inventory.model.Category;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryMapperTest {

    private final CategoryMapper mapper = new CategoryMapper();

    @Test
    void toEntity_mapsNameAndDescription() {
        CategoryCreateRequest req = new CategoryCreateRequest("Electronics", "Electronic items");

        Category result = mapper.toEntity(req);

        assertThat(result.getName()).isEqualTo("Electronics");
        assertThat(result.getDescription()).isEqualTo("Electronic items");
    }

    @Test
    void toResponse_mapsAllFields() {
        Category cat = new Category();
        cat.setId(UUID.randomUUID());
        cat.setName("Electronics");
        cat.setDescription("Electronic items");

        CategoryResponse response = mapper.toResponse(cat);

        assertThat(response.id()).isEqualTo(cat.getId());
        assertThat(response.name()).isEqualTo("Electronics");
        assertThat(response.description()).isEqualTo("Electronic items");
    }
}

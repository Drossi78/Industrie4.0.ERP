package com.stratiq.backend.inventory.mapper;

import com.stratiq.backend.inventory.dto.request.CategoryCreateRequest;
import com.stratiq.backend.inventory.dto.response.CategoryResponse;
import com.stratiq.backend.inventory.model.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category toEntity(CategoryCreateRequest req) {
        // Map category request to entity.
        Category c = new Category();
        c.setName(req.name());
        c.setDescription(req.description());
        return c;
    }

    public CategoryResponse toResponse(Category e) {
        // Transform category entity to response DTO.
        return new CategoryResponse(e.getId(), e.getCreatedAt(), e.getName(), e.getDescription());
    }
}

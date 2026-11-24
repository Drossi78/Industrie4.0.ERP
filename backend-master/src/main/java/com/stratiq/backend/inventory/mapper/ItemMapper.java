package com.stratiq.backend.inventory.mapper;

import com.stratiq.backend.inventory.dto.response.ItemResponse;
import com.stratiq.backend.inventory.model.Item;
import org.springframework.stereotype.Component;

@Component
public class ItemMapper {

    public ItemResponse toResponse(Item item) {
        // Transform entity to response DTO; includes category details (ID and name) if category assigned.
        return new ItemResponse(
            item.getId(),
            item.getCreatedAt(),
            item.getName(),
            item.getKind(),
            item.getCategory() != null ? item.getCategory().getId() : null,
            item.getCategory() != null ? item.getCategory().getName() : null,
            item.getUnit(),
            item.getPrice(),
            item.getDescription(),
            item.getNotes(),
            item.isDeleted()
        );
    }
}

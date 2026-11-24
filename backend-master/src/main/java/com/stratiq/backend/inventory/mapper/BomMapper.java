package com.stratiq.backend.inventory.mapper;

import com.stratiq.backend.inventory.dto.response.BomResponse;
import com.stratiq.backend.inventory.model.BillOfMaterial;
import org.springframework.stereotype.Component;

@Component
public class BomMapper {

    public BomResponse toResponse(BillOfMaterial bom) {
        // Transform BOM to response; includes parent item and component item names for manufacturing view.
        return new BomResponse(
            bom.getId(),
            bom.getCreatedAt(),
            bom.getItem() != null ? bom.getItem().getId() : null,
            bom.getItem() != null ? bom.getItem().getName() : null,
            bom.getComponent() != null ? bom.getComponent().getId() : null,
            bom.getComponent() != null ? bom.getComponent().getName() : null,
            bom.getQuantity(),
            bom.getUnit(),
            bom.getNotes()
        );
    }
}

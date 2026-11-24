package com.stratiq.backend.production.mapper;

import com.stratiq.backend.production.dto.request.ProductionStepCreateRequest;
import com.stratiq.backend.production.dto.request.ProductionStepUpdateRequest;
import com.stratiq.backend.production.dto.response.ProductionStepResponse;
import com.stratiq.backend.inventory.model.Item;
import com.stratiq.backend.production.model.ProductionStep;
import org.springframework.stereotype.Component;

@Component
public class ProductionStepMapper {

    public ProductionStep toEntity(ProductionStepCreateRequest req, Item item) {
        // Map to entity; stepOrder defines the manufacturing sequence position.
        ProductionStep ps = new ProductionStep();
        ps.setItem(item);
        ps.setName(req.name());
        ps.setDescription(req.description());
        ps.setStepOrder(req.stepOrder());
        ps.setNotes(req.notes());
        return ps;
    }

    public void update(ProductionStep entity, ProductionStepUpdateRequest req) {
        // Selective update: null fields skipped (supports soft-delete and order changes).
        if (req.name()        != null) entity.setName(req.name());
        if (req.description() != null) entity.setDescription(req.description());
        if (req.stepOrder()   != null) entity.setStepOrder(req.stepOrder());
        if (req.notes()       != null) entity.setNotes(req.notes());
        if (req.isDeleted()   != null) entity.setDeleted(req.isDeleted());
    }

    public ProductionStepResponse toResponse(ProductionStep e) {
        return new ProductionStepResponse(
                e.getId(), e.getCreatedAt(),
                e.getItem().getId(),
                e.getName(), e.getDescription(),
                e.getStepOrder(), e.isDeleted(),
                e.getNotes()
        );
    }
}

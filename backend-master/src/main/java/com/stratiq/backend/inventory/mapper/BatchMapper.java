package com.stratiq.backend.inventory.mapper;

import com.stratiq.backend.inventory.dto.response.BatchResponse;
import com.stratiq.backend.inventory.model.Batch;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class BatchMapper {

    // currentStock is derived from the batch's stock movements (not stored on the entity)
    // and supplied by the service; pass BigDecimal.ZERO for a batch with no movements.
    public BatchResponse toResponse(Batch batch, BigDecimal currentStock) {
        // Transform batch entity to response DTO; includes related Item and Warehouse names for UI display.
        return new BatchResponse(
            batch.getId(),
            batch.getCreatedAt(),
            batch.getBatchNumber(),
            batch.getItem() != null ? batch.getItem().getId() : null,
            batch.getItem() != null ? batch.getItem().getName() : null,
            batch.getWarehouse() != null ? batch.getWarehouse().getId() : null,
            batch.getWarehouse() != null ? batch.getWarehouse().getName() : null,
            batch.getExpiryDate(),
            currentStock
        );
    }
}

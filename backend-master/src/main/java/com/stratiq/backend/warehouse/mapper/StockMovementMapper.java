package com.stratiq.backend.warehouse.mapper;

import com.stratiq.backend.warehouse.dto.request.StockMovementCreateRequest;
import com.stratiq.backend.warehouse.dto.request.StockMovementUpdateRequest;
import com.stratiq.backend.warehouse.dto.response.StockMovementResponse;
import com.stratiq.backend.inventory.model.Item;
import com.stratiq.backend.order.model.Order;
import com.stratiq.backend.warehouse.model.StockMovement;
import com.stratiq.backend.warehouse.model.Warehouse;
import org.springframework.stereotype.Component;

@Component
public class StockMovementMapper {

    public StockMovement toEntity(StockMovementCreateRequest req,
                                  Item item, Order order, Warehouse warehouse) {
        // Map to entity; cross-domain references: Item (required), Order (optional), Warehouse (required).
        StockMovement m = new StockMovement();
        m.setNote(req.note());
        m.setQuantity(req.quantity());
        // Type: IN/OUT (English) or EINGANG/AUSGANG (legacy German terminology).
        m.setType(req.type());
        m.setItem(item);
        m.setOrder(order);
        m.setWarehouse(warehouse);
        return m;
    }

    public void update(StockMovement entity, StockMovementUpdateRequest req,
                       Item item, Order order, Warehouse warehouse) {
        // Selective update: null fields skipped (allows movement adjustments).
        if (req.note()        != null) entity.setNote(req.note());
        if (req.quantity()    != null) entity.setQuantity(req.quantity());
        if (req.type()        != null) entity.setType(req.type());
        if (req.itemId()      != null) entity.setItem(item);
        if (req.orderId()     != null) entity.setOrder(order);
        if (req.warehouseId() != null) entity.setWarehouse(warehouse);
    }

    public StockMovementResponse toResponse(StockMovement e) {
        return new StockMovementResponse(
                e.getId(),
                e.getCreatedAt(),
                e.getNote(),
                e.getQuantity(),
                e.getType(),
                e.getItem() != null ? e.getItem().getId() : null,
                e.getOrder() != null ? e.getOrder().getId() : null,
                e.getWarehouse() != null ? e.getWarehouse().getId() : null,
                e.getBatch() != null ? e.getBatch().getId() : null,
                e.getBatch() != null ? e.getBatch().getBatchNumber() : null,
                e.getBatch() != null ? e.getBatch().getExpiryDate() : null
        );
    }
}

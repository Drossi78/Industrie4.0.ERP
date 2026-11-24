package com.stratiq.backend.order.mapper;

import com.stratiq.backend.order.dto.request.OrderItemCreateRequest;
import com.stratiq.backend.order.dto.request.OrderItemUpdateRequest;
import com.stratiq.backend.order.dto.response.OrderItemResponse;
import com.stratiq.backend.order.model.Order;
import com.stratiq.backend.order.model.OrderItem;
import com.stratiq.backend.inventory.model.Item;
import com.stratiq.backend.inventory.model.Supplier;
import org.springframework.stereotype.Component;

@Component
public class OrderItemMapper {

    public OrderItem toEntity(OrderItemCreateRequest req,
                               Order order, Item item, Supplier supplier) {
        OrderItem oi = new OrderItem();
        // Map cross-domain references: Order (required), Item (required), Supplier (optional).
        oi.setOrder(order);
        oi.setItem(item);
        oi.setItemName(req.itemName());
        oi.setUnit(req.unit());
        oi.setQuantity(req.quantity());
        oi.setUnitPrice(req.unitPrice());
        oi.setNotes(req.notes());
        oi.setSupplier(supplier);
        return oi;
    }

    public void update(OrderItem entity, OrderItemUpdateRequest req, Supplier supplier) {
        // Selective update: null fields are skipped (allows partial line-item modifications).
        if (req.itemName()   != null) entity.setItemName(req.itemName());
        if (req.unit()       != null) entity.setUnit(req.unit());
        if (req.quantity()   != null) entity.setQuantity(req.quantity());
        if (req.unitPrice()  != null) entity.setUnitPrice(req.unitPrice());
        if (req.notes()      != null) entity.setNotes(req.notes());
        if (req.supplierId() != null) entity.setSupplier(supplier);
    }

    public OrderItemResponse toResponse(OrderItem e) {
        return new OrderItemResponse(
                e.getId(), e.getCreatedAt(),
                e.getOrder().getId(), e.getItem().getId(),
                e.getItemName(), e.getUnit(),
                e.getQuantity(), e.getUnitPrice(), e.getLineTotal(),
                e.getNotes(),
                e.getSupplier() != null ? e.getSupplier().getId() : null
        );
    }
}

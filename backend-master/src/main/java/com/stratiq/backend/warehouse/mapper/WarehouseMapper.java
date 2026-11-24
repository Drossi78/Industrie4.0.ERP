package com.stratiq.backend.warehouse.mapper;

import com.stratiq.backend.warehouse.dto.request.WarehouseCreateRequest;
import com.stratiq.backend.warehouse.dto.request.WarehouseUpdateRequest;
import com.stratiq.backend.warehouse.dto.response.WarehouseResponse;
import com.stratiq.backend.warehouse.model.Warehouse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class WarehouseMapper {

    public Warehouse toEntity(WarehouseCreateRequest req) {
        Warehouse w = new Warehouse();
        w.setName(req.name());
        w.setDescription(req.description());
        w.setStreet(req.street());
        w.setHouseNumber(req.houseNumber());
        w.setZipCode(req.zipCode());
        w.setCity(req.city());
        w.setState(req.state());
        w.setCountry(req.country());
        // Default: warehouse is active (operational) unless explicitly set to false.
        w.setActive(req.isActive() != null ? req.isActive() : true);
        w.setUpdatedAt(LocalDateTime.now());
        return w;
    }

    public void update(Warehouse entity, WarehouseUpdateRequest req) {
        // Selective update: null fields skipped (supports address updates and status toggles).
        if (req.name()        != null) entity.setName(req.name());
        if (req.description() != null) entity.setDescription(req.description());
        if (req.street()      != null) entity.setStreet(req.street());
        if (req.houseNumber() != null) entity.setHouseNumber(req.houseNumber());
        if (req.zipCode()     != null) entity.setZipCode(req.zipCode());
        if (req.city()        != null) entity.setCity(req.city());
        if (req.state()       != null) entity.setState(req.state());
        if (req.country()     != null) entity.setCountry(req.country());
        if (req.isActive()    != null) entity.setActive(req.isActive());
        // Update modification timestamp.
        entity.setUpdatedAt(LocalDateTime.now());
    }

    public WarehouseResponse toResponse(Warehouse e) {
        return new WarehouseResponse(
                e.getId(), e.getCreatedAt(),
                e.getName(), e.getDescription(),
                e.getStreet(), e.getHouseNumber(), e.getZipCode(),
                e.getCity(), e.getState(), e.getCountry(),
                e.isActive()
        );
    }
}

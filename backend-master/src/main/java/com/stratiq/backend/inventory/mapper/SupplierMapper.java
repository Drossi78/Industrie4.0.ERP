package com.stratiq.backend.inventory.mapper;

import com.stratiq.backend.inventory.dto.request.SupplierCreateRequest;
import com.stratiq.backend.inventory.dto.request.SupplierUpdateRequest;
import com.stratiq.backend.inventory.dto.response.SupplierResponse;
import com.stratiq.backend.inventory.model.Supplier;
import org.springframework.stereotype.Component;

@Component
public class SupplierMapper {

    public Supplier toEntity(SupplierCreateRequest req) {
        // Map request to entity; includes complete contact and address details.
        Supplier s = new Supplier();
        s.setName(req.name());
        s.setEmail(req.email());
        s.setPhone(req.phone());
        s.setTaxId(req.taxId());
        s.setStreet(req.street());
        s.setHouseNumber(req.houseNumber());
        s.setZipCode(req.zipCode());
        s.setCity(req.city());
        s.setState(req.state());
        s.setCountry(req.country());
        s.setNotes(req.notes());
        return s;
    }

    public void update(Supplier entity, SupplierUpdateRequest req) {
        // Selective update: null fields skipped (partial supplier profile updates).
        if (req.name()        != null) entity.setName(req.name());
        if (req.email()       != null) entity.setEmail(req.email());
        if (req.phone()       != null) entity.setPhone(req.phone());
        if (req.taxId()       != null) entity.setTaxId(req.taxId());
        if (req.street()      != null) entity.setStreet(req.street());
        if (req.houseNumber() != null) entity.setHouseNumber(req.houseNumber());
        if (req.zipCode()     != null) entity.setZipCode(req.zipCode());
        if (req.city()        != null) entity.setCity(req.city());
        if (req.state()       != null) entity.setState(req.state());
        if (req.country()     != null) entity.setCountry(req.country());
        if (req.notes()       != null) entity.setNotes(req.notes());
    }

    public SupplierResponse toResponse(Supplier e) {
        return new SupplierResponse(
                e.getId(), e.getCreatedAt(),
                e.getName(), e.getEmail(), e.getPhone(), e.getTaxId(),
                e.getStreet(), e.getHouseNumber(), e.getZipCode(),
                e.getCity(), e.getState(), e.getCountry(),
                e.getNotes()
        );
    }
}

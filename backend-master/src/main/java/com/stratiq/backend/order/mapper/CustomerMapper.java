package com.stratiq.backend.order.mapper;

import com.stratiq.backend.order.dto.request.CustomerCreateRequest;
import com.stratiq.backend.order.dto.request.CustomerUpdateRequest;
import com.stratiq.backend.order.dto.response.CustomerResponse;
import com.stratiq.backend.order.model.Customer;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CustomerMapper {

    public Customer toEntity(CustomerCreateRequest req) {
        // Map request to entity; includes complete billing and contact address information.
        Customer c = new Customer();
        c.setName(req.name());
        c.setEmail(req.email());
        c.setPhone(req.phone());
        c.setTaxId(req.taxId());
        c.setStreet(req.street());
        c.setHouseNumber(req.houseNumber());
        c.setZipCode(req.zipCode());
        c.setCity(req.city());
        c.setState(req.state());
        c.setCountry(req.country());
        c.setNotes(req.notes());
        // Audit timestamp: captures last modification time.
        c.setUpdatedAt(LocalDateTime.now());
        return c;
    }

    public void update(Customer entity, CustomerUpdateRequest req) {
        // Selective update: null fields skipped (partial customer profile updates).
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
        // Update modification timestamp.
        entity.setUpdatedAt(LocalDateTime.now());
    }

    public CustomerResponse toResponse(Customer e) {
        return new CustomerResponse(
                e.getId(), e.getCreatedAt(),
                e.getName(), e.getEmail(), e.getPhone(), e.getTaxId(),
                e.getStreet(), e.getHouseNumber(), e.getZipCode(),
                e.getCity(), e.getState(), e.getCountry(),
                e.getNotes()
        );
    }
}

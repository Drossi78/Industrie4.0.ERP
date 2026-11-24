package com.stratiq.backend.inventory.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record SupplierResponse(
        UUID id,
        LocalDateTime createdAt,
        String name,
        String email,
        String phone,
        String taxId,
        String street,
        String houseNumber,
        String zipCode,
        String city,
        String state,
        String country,
        String notes
) {}

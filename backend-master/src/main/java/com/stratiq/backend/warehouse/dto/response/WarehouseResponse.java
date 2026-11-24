package com.stratiq.backend.warehouse.dto.response;

import java.time.Instant;
import java.util.UUID;

public record WarehouseResponse(
        UUID id,
        Instant createdAt,
        String name,
        String description,
        String street,
        String houseNumber,
        String zipCode,
        String city,
        String state,
        String country,
        boolean isActive
) {}

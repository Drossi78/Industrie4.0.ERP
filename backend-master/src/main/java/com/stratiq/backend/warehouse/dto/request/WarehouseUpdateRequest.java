package com.stratiq.backend.warehouse.dto.request;

import jakarta.validation.constraints.Size;

public record WarehouseUpdateRequest(
        @Size(max = 255) String name,
        String description,
        @Size(max = 255) String street,
        @Size(max = 20) String houseNumber,
        @Size(max = 20) String zipCode,
        @Size(max = 100) String city,
        @Size(max = 100) String state,
        @Size(max = 100) String country,
        Boolean isActive
) {}

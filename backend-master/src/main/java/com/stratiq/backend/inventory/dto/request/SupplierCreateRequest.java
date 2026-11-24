package com.stratiq.backend.inventory.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SupplierCreateRequest(
        @NotBlank @Size(max = 255) String name,
        @Email @Size(max = 255) String email,
        @Size(max = 50) String phone,
        @Size(max = 50) String taxId,
        @Size(max = 255) String street,
        @Size(max = 20) String houseNumber,
        @Size(max = 20) String zipCode,
        @Size(max = 100) String city,
        @Size(max = 100) String state,
        @Size(max = 100) String country,
        String notes
) {}

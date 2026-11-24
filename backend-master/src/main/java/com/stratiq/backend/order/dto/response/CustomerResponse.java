package com.stratiq.backend.order.dto.response;

import java.time.Instant;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        Instant createdAt,
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

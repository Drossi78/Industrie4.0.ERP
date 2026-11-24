package com.stratiq.backend.inventory.dto.response;

import java.time.Instant;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        Instant createdAt,
        String name,
        String description
) {}

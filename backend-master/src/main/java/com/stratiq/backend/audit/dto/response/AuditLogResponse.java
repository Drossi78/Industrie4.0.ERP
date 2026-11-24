package com.stratiq.backend.audit.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        LocalDateTime createdAt,
        UUID userId,
        String entityType,
        UUID entityId,
        String action,
        String oldValue,
        String newValue
) {}

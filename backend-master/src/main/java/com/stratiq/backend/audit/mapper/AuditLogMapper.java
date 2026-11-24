package com.stratiq.backend.audit.mapper;

import com.stratiq.backend.audit.dto.response.AuditLogResponse;
import com.stratiq.backend.audit.model.AuditLog;
import org.springframework.stereotype.Component;

@Component
public class AuditLogMapper {

    public AuditLogResponse toResponse(AuditLog log) {
        // Transform audit log entity to response DTO (captures user action, entity, and value changes).
        return new AuditLogResponse(
            log.getId(),
            log.getCreatedAt(),
            log.getUserId(),
            log.getEntityType(),
            log.getEntityId(),
            log.getAction(),
            log.getOldValue(),
            log.getNewValue()
        );
    }
}

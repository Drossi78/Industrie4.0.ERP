package com.stratiq.backend.audit.mapper;

import com.stratiq.backend.audit.dto.response.AuditLogResponse;
import com.stratiq.backend.audit.model.AuditLog;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AuditLogMapperTest {

    private final AuditLogMapper mapper = new AuditLogMapper();

    @Test
    void toResponse_mapsAllFields() {
        AuditLog log = new AuditLog();
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        log.setId(id);
        log.setCreatedAt(now);
        log.setUserId(userId);
        log.setEntityType("ORDER");
        log.setEntityId(entityId);
        log.setAction("CREATE");
        log.setOldValue(null);
        log.setNewValue("{\"status\":\"DRAFT\"}");

        AuditLogResponse response = mapper.toResponse(log);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.createdAt()).isEqualTo(now);
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.entityType()).isEqualTo("ORDER");
        assertThat(response.entityId()).isEqualTo(entityId);
        assertThat(response.action()).isEqualTo("CREATE");
        assertThat(response.oldValue()).isNull();
        assertThat(response.newValue()).isEqualTo("{\"status\":\"DRAFT\"}");
    }
}

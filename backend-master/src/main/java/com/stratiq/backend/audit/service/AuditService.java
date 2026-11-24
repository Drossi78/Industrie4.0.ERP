package com.stratiq.backend.audit.service;

import com.stratiq.backend.audit.dto.response.AuditLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AuditService {
    Page<AuditLogResponse> findAll(Pageable pageable);
    Page<AuditLogResponse> findByEntity(String entityType, UUID entityId, Pageable pageable);
    Page<AuditLogResponse> findByUser(UUID userId, Pageable pageable);
    void log(UUID userId, String entityType, UUID entityId, String action, String oldValue, String newValue);
}

package com.stratiq.backend.audit.service;

import com.stratiq.backend.audit.dto.response.AuditLogResponse;
import com.stratiq.backend.audit.mapper.AuditLogMapper;
import com.stratiq.backend.audit.model.AuditLog;
import com.stratiq.backend.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository repo;
    private final AuditLogMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> findAll(Pageable pageable) {
        return repo.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> findByEntity(String entityType, UUID entityId, Pageable pageable) {
        return repo.findByEntityTypeAndEntityId(entityType, entityId, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> findByUser(UUID userId, Pageable pageable) {
        return repo.findByUserId(userId, pageable).map(mapper::toResponse);
    }

    @Override
    public void log(UUID userId, String entityType, UUID entityId, String action, String oldValue, String newValue) {
        // Called by AuditingAspect for mutation tracking. Captures user, action, and old/new values.
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setAction(action);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        repo.save(log);
    }
}

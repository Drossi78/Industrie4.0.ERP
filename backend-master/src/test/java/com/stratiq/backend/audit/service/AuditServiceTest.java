package com.stratiq.backend.audit.service;

import com.stratiq.backend.audit.mapper.AuditLogMapper;
import com.stratiq.backend.audit.model.AuditLog;
import com.stratiq.backend.audit.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private AuditLogMapper auditLogMapper;

    @InjectMocks
    private AuditServiceImpl auditService;

    private UUID testUserId;
    private UUID testEntityId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testEntityId = UUID.randomUUID();
    }

    @Test
    void testLogCreateEvent_Success() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // Act
        auditService.log(testUserId, "ORDER", testEntityId, "CREATE", null, "{\"id\":\"123\"}");

        // Assert
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    void testLogUpdateEvent_WithOldAndNewValues() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // Act
        auditService.log(testUserId, "ITEM", testEntityId, "UPDATE",
            "{\"price\":\"100\"}", "{\"price\":\"150\"}");

        // Assert
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    void testLogDeleteEvent_Success() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());

        // Act
        auditService.log(testUserId, "ITEM", testEntityId, "DELETE",
            "{\"name\":\"Item1\"}", null);

        // Assert
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    void testFindAll_Success() {
        // Arrange
        var pageable = PageRequest.of(0, 10);
        when(auditLogRepository.findAll(pageable)).thenReturn(mock(org.springframework.data.domain.Page.class));

        // Act
        auditService.findAll(pageable);

        // Assert
        verify(auditLogRepository, times(1)).findAll(pageable);
    }

    @Test
    void testFindByEntity_Success() {
        // Arrange
        var pageable = PageRequest.of(0, 10);
        when(auditLogRepository.findByEntityTypeAndEntityId("ORDER", testEntityId, pageable))
            .thenReturn(mock(org.springframework.data.domain.Page.class));

        // Act
        auditService.findByEntity("ORDER", testEntityId, pageable);

        // Assert
        verify(auditLogRepository, times(1))
            .findByEntityTypeAndEntityId("ORDER", testEntityId, pageable);
    }

    @Test
    void testFindByUser_Success() {
        // Arrange
        var pageable = PageRequest.of(0, 10);
        when(auditLogRepository.findByUserId(testUserId, pageable))
            .thenReturn(mock(org.springframework.data.domain.Page.class));

        // Act
        auditService.findByUser(testUserId, pageable);

        // Assert
        verify(auditLogRepository, times(1)).findByUserId(testUserId, pageable);
    }
}

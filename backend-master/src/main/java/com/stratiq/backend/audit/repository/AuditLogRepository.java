package com.stratiq.backend.audit.repository;

import com.stratiq.backend.audit.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    // Find all audit logs by entity type with pagination (e.g., "Order", "Item" for audit trails).
    Page<AuditLog> findByEntityType(String entityType, Pageable pageable);
    // Find all mutations performed by a specific user (user activity history).
    Page<AuditLog> findByUserId(UUID userId, Pageable pageable);
    // Find all changes to a specific entity instance (detailed change history for one record).
    Page<AuditLog> findByEntityTypeAndEntityId(String entityType, UUID entityId, Pageable pageable);
}

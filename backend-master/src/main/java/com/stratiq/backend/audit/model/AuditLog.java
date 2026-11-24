package com.stratiq.backend.audit.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(nullable = false, updatable = false)
    private UUID id;

    // User who performed the action (null if unauthenticated).
    @Column(name = "user_id")
    private UUID userId;

    // Entity type (e.g., "ORDER", "ITEM", "USER") - logged by AuditingAspect.
    @Column(name = "entity_type", length = 100, nullable = false)
    private String entityType;

    // ID of the entity that was modified (null for creation without specific entity).
    @Column(name = "entity_id")
    private UUID entityId;

    // Action: CREATE, UPDATE, DELETE - captured from service method name.
    @Column(length = 50, nullable = false)
    private String action;

    @Column(name = "old_value", columnDefinition = "text")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "text")
    private String newValue;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

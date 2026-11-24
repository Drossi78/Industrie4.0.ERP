package com.stratiq.backend.audit.controller;

import com.stratiq.backend.audit.dto.response.AuditLogResponse;
import com.stratiq.backend.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService service;

    @GetMapping
    @PreAuthorize("hasAuthority('audit:read')")
    public ResponseEntity<Page<AuditLogResponse>> list(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) UUID entityId,
            @RequestParam(required = false) UUID userId,
            Pageable pageable) {
        if (userId != null) {
            return ResponseEntity.ok(service.findByUser(userId, pageable));
        }
        if (entityType != null && entityId != null) {
            return ResponseEntity.ok(service.findByEntity(entityType, entityId, pageable));
        }
        return ResponseEntity.ok(service.findAll(pageable));
    }
}

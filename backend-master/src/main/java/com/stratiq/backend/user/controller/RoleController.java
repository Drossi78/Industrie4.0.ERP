package com.stratiq.backend.user.controller;

import com.stratiq.backend.user.dto.request.PermissionAssignRequest;
import com.stratiq.backend.user.dto.request.RoleCreateRequest;
import com.stratiq.backend.user.dto.request.RoleUpdateRequest;
import com.stratiq.backend.user.dto.response.RoleResponse;
import com.stratiq.backend.user.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService service;

    @GetMapping
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<List<RoleResponse>> list() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<RoleResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('admin:access')")
    public ResponseEntity<RoleResponse> create(@Valid @RequestBody RoleCreateRequest req) {
        return ResponseEntity.status(201).body(service.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('admin:access')")
    public ResponseEntity<RoleResponse> update(@PathVariable UUID id, @Valid @RequestBody RoleUpdateRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @PostMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('admin:access')")
    public ResponseEntity<RoleResponse> assignPermissions(@PathVariable UUID id,
                                                          @RequestBody PermissionAssignRequest req) {
        return ResponseEntity.ok(service.assignPermissions(id, req));
    }

    @DeleteMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('admin:access')")
    public ResponseEntity<RoleResponse> revokePermissions(@PathVariable UUID id,
                                                          @RequestBody PermissionAssignRequest req) {
        return ResponseEntity.ok(service.revokePermissions(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('admin:access')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

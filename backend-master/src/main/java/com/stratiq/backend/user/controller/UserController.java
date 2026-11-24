package com.stratiq.backend.user.controller;

import com.stratiq.backend.user.dto.request.UserChangeRoleRequest;
import com.stratiq.backend.user.dto.request.UserCreateRequest;
import com.stratiq.backend.user.dto.request.UserUpdateRequest;
import com.stratiq.backend.user.dto.response.UserResponse;
import com.stratiq.backend.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @GetMapping
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<?> list(
            @RequestParam(required = false) Boolean active,
            Pageable pageable) {
        if (Boolean.TRUE.equals(active)) {
            List<UserResponse> activeUsers = service.findAllActive();
            return ResponseEntity.ok(activeUsers);
        }
        return ResponseEntity.ok(service.findAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<UserResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest req) {
        return ResponseEntity.status(201).body(service.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<UserResponse> update(@PathVariable UUID id, @Valid @RequestBody UserUpdateRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<UserResponse> assignRole(@PathVariable UUID id, @RequestBody UserChangeRoleRequest req) {
        return ResponseEntity.ok(service.assignRole(id, req.roleId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user:delete')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

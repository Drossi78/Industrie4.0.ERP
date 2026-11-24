package com.stratiq.backend.user.controller;

import com.stratiq.backend.user.dto.request.PermissionCreateRequest;
import com.stratiq.backend.user.dto.response.PermissionResponse;
import com.stratiq.backend.user.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {
    // Permissions are immutable system definitions — no update/delete endpoints.

    private final PermissionService service;

    @GetMapping
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<List<PermissionResponse>> list() {
        return ResponseEntity.ok(service.findAll());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('admin:access')")
    public ResponseEntity<PermissionResponse> create(@Valid @RequestBody PermissionCreateRequest req) {
        return ResponseEntity.status(201).body(service.create(req));
    }
}

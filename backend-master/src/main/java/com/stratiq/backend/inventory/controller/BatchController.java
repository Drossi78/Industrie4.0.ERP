package com.stratiq.backend.inventory.controller;

import com.stratiq.backend.inventory.dto.request.BatchCreateRequest;
import com.stratiq.backend.inventory.dto.request.BatchUpdateRequest;
import com.stratiq.backend.inventory.dto.response.BatchResponse;
import com.stratiq.backend.inventory.service.BatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/batches")
@RequiredArgsConstructor
public class BatchController {

    private final BatchService service;

    @GetMapping
    @PreAuthorize("hasAuthority('inventory:read')")
    public ResponseEntity<List<BatchResponse>> list(
            @RequestParam(required = false) UUID itemId,
            @RequestParam(required = false) UUID warehouseId) {
        if (itemId != null)      return ResponseEntity.ok(service.findByItem(itemId));
        if (warehouseId != null) return ResponseEntity.ok(service.findByWarehouse(warehouseId));
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory:read')")
    public ResponseEntity<BatchResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('inventory:write')")
    public ResponseEntity<BatchResponse> create(@Valid @RequestBody BatchCreateRequest req) {
        return ResponseEntity.status(201).body(service.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory:write')")
    public ResponseEntity<BatchResponse> update(@PathVariable UUID id, @Valid @RequestBody BatchUpdateRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory:delete')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

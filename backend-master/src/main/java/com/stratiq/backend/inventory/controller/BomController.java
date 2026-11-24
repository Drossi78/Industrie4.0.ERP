package com.stratiq.backend.inventory.controller;

import com.stratiq.backend.inventory.dto.request.BomCreateRequest;
import com.stratiq.backend.inventory.dto.response.BomResponse;
import com.stratiq.backend.inventory.service.BomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bom")
@RequiredArgsConstructor
public class BomController {

    private final BomService service;

    @GetMapping
    @PreAuthorize("hasAuthority('inventory:read')")
    public ResponseEntity<List<BomResponse>> list(@RequestParam(required = false) UUID itemId) {
        if (itemId != null) return ResponseEntity.ok(service.findByItem(itemId));
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory:read')")
    public ResponseEntity<BomResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('inventory:write')")
    public ResponseEntity<BomResponse> create(@Valid @RequestBody BomCreateRequest req) {
        return ResponseEntity.status(201).body(service.addComponent(req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory:delete')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

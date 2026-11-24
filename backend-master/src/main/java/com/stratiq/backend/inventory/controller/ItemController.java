package com.stratiq.backend.inventory.controller;

import com.stratiq.backend.inventory.dto.request.ItemCreateRequest;
import com.stratiq.backend.inventory.dto.request.ItemUpdateRequest;
import com.stratiq.backend.inventory.dto.response.ItemResponse;
import com.stratiq.backend.inventory.model.ItemKind;
import com.stratiq.backend.inventory.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService service;

    @GetMapping
    @PreAuthorize("hasAuthority('inventory:read')")
    public ResponseEntity<List<ItemResponse>> list(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) ItemKind kind) {
        // Filters checked in order: categoryId > kind > all items. First match wins.
        if (categoryId != null) {
            return ResponseEntity.ok(service.findByCategory(categoryId));
        }
        if (kind != null) {
            return ResponseEntity.ok(service.findByKind(kind));
        }
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory:read')")
    public ResponseEntity<ItemResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('inventory:write')")
    public ResponseEntity<ItemResponse> create(@Valid @RequestBody ItemCreateRequest req) {
        return ResponseEntity.status(201).body(service.create(req));
    }

    // Accept both PUT and PATCH: the update is a partial update (only non-null
    // fields are applied), and the web client issues PATCH.
    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    @PreAuthorize("hasAuthority('inventory:write')")
    public ResponseEntity<ItemResponse> update(@PathVariable UUID id, @Valid @RequestBody ItemUpdateRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory:delete')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

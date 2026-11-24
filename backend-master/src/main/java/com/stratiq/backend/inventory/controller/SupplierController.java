package com.stratiq.backend.inventory.controller;

import com.stratiq.backend.inventory.dto.request.SupplierCreateRequest;
import com.stratiq.backend.inventory.dto.request.SupplierUpdateRequest;
import com.stratiq.backend.inventory.dto.response.SupplierResponse;
import com.stratiq.backend.inventory.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static org.springframework.data.domain.Sort.Direction.DESC;

@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
@Tag(name = "Suppliers")
public class SupplierController {

    private final SupplierService service;

    @GetMapping
    @PreAuthorize("hasAuthority('inventory:read')")
    @Operation(summary = "List suppliers — filter by name (optional)")
    public Object list(
            @RequestParam(required = false) String name,
            @PageableDefault(size = 20, sort = "createdAt", direction = DESC) Pageable pageable) {
        if (name != null) return service.search(name);
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory:read')")
    public SupplierResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('inventory:write')")
    @ResponseStatus(HttpStatus.CREATED)
    public SupplierResponse create(@Valid @RequestBody SupplierCreateRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory:write')")
    public SupplierResponse update(@PathVariable UUID id, @Valid @RequestBody SupplierUpdateRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory:delete')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}

package com.stratiq.backend.warehouse.controller;

import com.stratiq.backend.warehouse.dto.request.WarehouseCreateRequest;
import com.stratiq.backend.warehouse.dto.request.WarehouseUpdateRequest;
import com.stratiq.backend.warehouse.dto.response.WarehouseResponse;
import com.stratiq.backend.warehouse.service.WarehouseService;
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
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
@Tag(name = "Warehouses")
public class WarehouseController {

    private final WarehouseService service;

    @GetMapping
    @PreAuthorize("hasAuthority('warehouse:read')")
    @Operation(summary = "List warehouses — filter by city or active=true (optional)")
    public Object list(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = "createdAt", direction = DESC) Pageable pageable) {
        if (city != null)                return service.findByCity(city);
        if (Boolean.TRUE.equals(active)) return service.findActive();
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('warehouse:read')")
    public WarehouseResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('warehouse:write')")
    @ResponseStatus(HttpStatus.CREATED)
    public WarehouseResponse create(@Valid @RequestBody WarehouseCreateRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('warehouse:write')")
    public WarehouseResponse update(@PathVariable UUID id, @Valid @RequestBody WarehouseUpdateRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('warehouse:delete')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}

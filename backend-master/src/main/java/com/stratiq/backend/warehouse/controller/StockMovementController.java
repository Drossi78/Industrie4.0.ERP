package com.stratiq.backend.warehouse.controller;

import com.stratiq.backend.warehouse.dto.request.StockMovementCreateRequest;
import com.stratiq.backend.warehouse.dto.request.StockMovementUpdateRequest;
import com.stratiq.backend.warehouse.dto.response.StockMovementResponse;
import com.stratiq.backend.warehouse.service.StockMovementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stock-movements")
@RequiredArgsConstructor
@Tag(name = "Stock Movements")
public class StockMovementController {

    private final StockMovementService service;

    @GetMapping
    @PreAuthorize("hasAuthority('warehouse:read')")
    @Operation(summary = "List stock movements — filter by itemId, warehouseId, orderId, or type (optional)")
    public Object list(
            @RequestParam(required = false) UUID itemId,
            @RequestParam(required = false) UUID warehouseId,
            @RequestParam(required = false) UUID orderId,
            @RequestParam(required = false) String type,
            Pageable pageable) {
        if (itemId != null)       return service.findByItem(itemId);
        if (warehouseId != null) return service.findByWarehouse(warehouseId);
        if (orderId != null)     return service.findByOrder(orderId);
        if (type != null)        return service.findByType(type);
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('warehouse:read')")
    public StockMovementResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('warehouse:write')")
    @ResponseStatus(HttpStatus.CREATED)
    public StockMovementResponse create(@Valid @RequestBody StockMovementCreateRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('warehouse:write')")
    public StockMovementResponse update(@PathVariable UUID id,
                                        @Valid @RequestBody StockMovementUpdateRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('warehouse:delete')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}

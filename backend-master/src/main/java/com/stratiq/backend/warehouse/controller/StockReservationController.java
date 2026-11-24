package com.stratiq.backend.warehouse.controller;

import com.stratiq.backend.warehouse.dto.request.CreateReservationRequest;
import com.stratiq.backend.warehouse.dto.response.ReservationResponse;
import com.stratiq.backend.warehouse.service.StockReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stock/reservations")
@RequiredArgsConstructor
@Tag(name = "Stock Reservations")
public class StockReservationController {

    private final StockReservationService service;

    @GetMapping
    @PreAuthorize("hasAuthority('warehouse:read')")
    @Operation(summary = "List reservations, optionally filtered by warehouse, item and status (default ACTIVE)")
    public List<ReservationResponse> list(
            @RequestParam(required = false) UUID warehouseId,
            @RequestParam(required = false) UUID itemId,
            @RequestParam(required = false) String status) {
        return service.list(warehouseId, itemId, status);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('warehouse:write')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Reserve stock (fails if available stock is insufficient)")
    public ReservationResponse create(@Valid @RequestBody CreateReservationRequest req) {
        return service.create(req);
    }

    @PostMapping("/{id}/release")
    @PreAuthorize("hasAuthority('warehouse:write')")
    @Operation(summary = "Release an active reservation, freeing the blocked stock")
    public ReservationResponse release(@PathVariable UUID id) {
        return service.release(id);
    }
}

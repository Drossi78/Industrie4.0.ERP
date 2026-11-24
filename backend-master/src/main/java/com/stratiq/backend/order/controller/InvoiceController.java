package com.stratiq.backend.order.controller;

import com.stratiq.backend.order.dto.request.InvoiceCreateRequest;
import com.stratiq.backend.order.dto.request.InvoiceUpdateRequest;
import com.stratiq.backend.order.dto.response.InvoiceResponse;
import com.stratiq.backend.order.model.InvoiceStatus;
import com.stratiq.backend.order.service.InvoiceService;
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
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoices")
public class InvoiceController {

    private final InvoiceService service;

    @GetMapping
    @PreAuthorize("hasAuthority('order:read')")
    @Operation(summary = "List invoices — filter by orderId or status (optional)")
    public Object list(
            @RequestParam(required = false) UUID orderId,
            @RequestParam(required = false) InvoiceStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = DESC) Pageable pageable) {
        if (orderId != null) return service.findByOrder(orderId);
        if (status != null)  return service.findByStatus(status);
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('order:read')")
    public InvoiceResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('order:write')")
    @ResponseStatus(HttpStatus.CREATED)
    public InvoiceResponse create(@Valid @RequestBody InvoiceCreateRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('order:write')")
    public InvoiceResponse update(@PathVariable UUID id, @Valid @RequestBody InvoiceUpdateRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('order:delete')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}

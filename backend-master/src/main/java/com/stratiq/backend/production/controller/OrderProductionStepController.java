package com.stratiq.backend.production.controller;

import com.stratiq.backend.production.dto.request.OrderProductionStepCreateRequest;
import com.stratiq.backend.production.dto.request.OrderProductionStepUpdateRequest;
import com.stratiq.backend.production.dto.response.OrderProductionStepResponse;
import com.stratiq.backend.production.service.OrderProductionStepService;
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

import static org.springframework.data.domain.Sort.Direction.ASC;

@RestController
@RequestMapping("/api/v1/order-production-steps")
@RequiredArgsConstructor
@Tag(name = "Order Production Steps")
public class OrderProductionStepController {

    private final OrderProductionStepService service;

    @GetMapping
    @PreAuthorize("hasAuthority('production:read')")
    @Operation(summary = "List order production steps — filter by orderId (optional)")
    public Object list(
            @RequestParam(required = false) UUID orderId,
            @PageableDefault(size = 20, sort = "stepOrder", direction = ASC) Pageable pageable) {
        if (orderId != null) return service.findByOrder(orderId);
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('production:read')")
    public OrderProductionStepResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('production:write')")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderProductionStepResponse create(@Valid @RequestBody OrderProductionStepCreateRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('production:write')")
    public OrderProductionStepResponse update(@PathVariable UUID id,
                                               @Valid @RequestBody OrderProductionStepUpdateRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('production:delete')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}

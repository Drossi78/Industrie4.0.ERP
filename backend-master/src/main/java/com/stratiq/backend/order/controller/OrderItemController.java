package com.stratiq.backend.order.controller;

import com.stratiq.backend.order.dto.request.OrderItemCreateRequest;
import com.stratiq.backend.order.dto.request.OrderItemUpdateRequest;
import com.stratiq.backend.order.dto.response.OrderItemResponse;
import com.stratiq.backend.order.service.OrderItemService;
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
@RequestMapping("/api/v1/order-items")
@RequiredArgsConstructor
@Tag(name = "Order Items")
public class OrderItemController {

    private final OrderItemService service;

    @GetMapping
    @PreAuthorize("hasAuthority('order:read')")
    @Operation(summary = "List order items — filter by orderId (optional)")
    public Object list(
            @RequestParam(required = false) UUID orderId,
            @PageableDefault(size = 20, sort = "createdAt", direction = DESC) Pageable pageable) {
        if (orderId != null) return service.findByOrder(orderId);
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('order:read')")
    public OrderItemResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('order:write')")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderItemResponse create(@Valid @RequestBody OrderItemCreateRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('order:write')")
    public OrderItemResponse update(@PathVariable UUID id, @Valid @RequestBody OrderItemUpdateRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('order:delete')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}

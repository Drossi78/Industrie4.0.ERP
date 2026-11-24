package com.stratiq.backend.order.controller;

import com.stratiq.backend.order.dto.request.OrderCreateRequest;
import com.stratiq.backend.order.dto.request.OrderUpdateRequest;
import com.stratiq.backend.order.dto.response.OrderItemResponse;
import com.stratiq.backend.order.dto.response.OrderResponse;
import com.stratiq.backend.order.model.OrderDirection;
import com.stratiq.backend.order.model.OrderStatus;
import com.stratiq.backend.order.service.OrderItemService;
import com.stratiq.backend.order.service.OrderService;
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

import java.util.List;
import java.util.UUID;

import static org.springframework.data.domain.Sort.Direction.DESC;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders")
public class OrderController {

    private final OrderService service;
    private final OrderItemService orderItemService;
    private final OrderProductionStepService orderProductionStepService;

    @GetMapping
    @PreAuthorize("hasAuthority('order:read')")
    @Operation(summary = "List orders — filter by customerId, status, or direction (optional)")
    public Object list(
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) OrderDirection direction,
            @PageableDefault(size = 20, sort = "createdAt", direction = DESC) Pageable pageable) {
        if (customerId != null) return service.findByCustomer(customerId);
        if (status != null)     return service.findByStatus(status);
        if (direction != null)  return service.findByDirection(direction);
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('order:read')")
    public OrderResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('order:write')")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody OrderCreateRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('order:write')")
    public OrderResponse update(@PathVariable UUID id, @Valid @RequestBody OrderUpdateRequest req) {
        return service.update(id, req);
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAuthority('order:write')")
    @Operation(summary = "Confirm the order and reserve BOM components for any OWN_PRODUCT line")
    public OrderResponse confirm(@PathVariable UUID id) {
        return service.confirm(id);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('order:write')")
    @Operation(summary = "Cancel the order and release any reservations made for it")
    public OrderResponse cancel(@PathVariable UUID id) {
        return service.cancel(id);
    }

    @GetMapping("/{id}/items")
    @PreAuthorize("hasAuthority('order:read')")
    public List<OrderItemResponse> getItems(@PathVariable UUID id) {
        return orderItemService.findByOrder(id);
    }

    @GetMapping("/{orderId}/items/{itemId}/production")
    @PreAuthorize("hasAuthority('production:read')")
    public List<OrderProductionStepResponse> getItemProductionSteps(
            @PathVariable UUID orderId, @PathVariable UUID itemId) {
        return orderProductionStepService.findByOrderItem(itemId);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('order:delete')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}

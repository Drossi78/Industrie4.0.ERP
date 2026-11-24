package com.stratiq.backend.warehouse.controller;

import com.stratiq.backend.warehouse.dto.request.OrderFulfillRequest;
import com.stratiq.backend.warehouse.dto.response.StockMovementResponse;
import com.stratiq.backend.warehouse.service.OrderFulfillmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// Bridges orders and stock: books movements for an order's items, linked to the order.
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order Fulfillment")
public class OrderFulfillmentController {

    private final OrderFulfillmentService service;

    @PostMapping("/{orderId}/fulfill")
    @PreAuthorize("hasAuthority('warehouse:write')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Book stock movements for all order items (direction IN or OUT), linked to the order")
    public List<StockMovementResponse> fulfill(@PathVariable UUID orderId,
                                               @Valid @RequestBody OrderFulfillRequest req) {
        return service.fulfill(orderId, req);
    }
}

package com.stratiq.backend.production.controller;

import com.stratiq.backend.production.dto.request.ProductionStepPatchRequest;
import com.stratiq.backend.production.dto.response.OrderProductionStepResponse;
import com.stratiq.backend.production.dto.response.ProductionOrderDetailResponse;
import com.stratiq.backend.production.dto.response.ProductionOrderSummaryResponse;
import com.stratiq.backend.production.service.OrderProductionStepService;
import com.stratiq.backend.production.service.ProductionDashboardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/production")
@RequiredArgsConstructor
@Tag(name = "Production Dashboard")
public class ProductionDashboardController {

    private final ProductionDashboardService dashboardService;
    private final OrderProductionStepService stepService;

    @GetMapping("/orders")
    @PreAuthorize("hasAuthority('production:read')")
    public List<ProductionOrderSummaryResponse> getActiveOrders() {
        return dashboardService.getActiveOrders();
    }

    @GetMapping("/orders/{id}")
    @PreAuthorize("hasAuthority('production:read')")
    public ProductionOrderDetailResponse getOrderDetail(@PathVariable UUID id) {
        return dashboardService.getOrderDetail(id);
    }

    @PatchMapping("/steps/{stepId}")
    @PreAuthorize("hasAuthority('production:write')")
    public OrderProductionStepResponse patchStep(
            @PathVariable UUID stepId,
            @RequestBody ProductionStepPatchRequest req) {
        return stepService.patch(stepId, req);
    }
}

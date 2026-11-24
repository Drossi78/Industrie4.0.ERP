package com.stratiq.backend.production.service;

import com.stratiq.backend.production.dto.response.ProductionOrderDetailResponse;
import com.stratiq.backend.production.dto.response.ProductionOrderSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface ProductionDashboardService {
    List<ProductionOrderSummaryResponse> getActiveOrders();
    ProductionOrderDetailResponse getOrderDetail(UUID orderId);
}

package com.stratiq.backend.production.service;

import com.stratiq.backend.production.dto.request.OrderProductionStepCreateRequest;
import com.stratiq.backend.production.dto.request.OrderProductionStepUpdateRequest;
import com.stratiq.backend.production.dto.request.ProductionStepPatchRequest;
import com.stratiq.backend.production.dto.response.OrderProductionStepResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface OrderProductionStepService {
    OrderProductionStepResponse create(OrderProductionStepCreateRequest req);
    OrderProductionStepResponse update(UUID id, OrderProductionStepUpdateRequest req);
    OrderProductionStepResponse get(UUID id);
    Page<OrderProductionStepResponse> list(Pageable pageable);
    List<OrderProductionStepResponse> findByOrder(UUID orderId);
    List<OrderProductionStepResponse> findByOrderItem(UUID orderItemId);
    OrderProductionStepResponse patch(UUID id, ProductionStepPatchRequest req);
    void delete(UUID id);
}

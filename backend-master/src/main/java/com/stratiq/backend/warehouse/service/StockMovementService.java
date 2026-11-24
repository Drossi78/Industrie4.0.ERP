package com.stratiq.backend.warehouse.service;

import com.stratiq.backend.warehouse.dto.request.StockMovementCreateRequest;
import com.stratiq.backend.warehouse.dto.request.StockMovementUpdateRequest;
import com.stratiq.backend.warehouse.dto.response.StockMovementResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface StockMovementService {
    StockMovementResponse create(StockMovementCreateRequest req);
    StockMovementResponse update(UUID id, StockMovementUpdateRequest req);
    StockMovementResponse get(UUID id);
    Page<StockMovementResponse> list(Pageable pageable);
    List<StockMovementResponse> findByItem(UUID itemId);
    List<StockMovementResponse> findByWarehouse(UUID warehouseId);
    List<StockMovementResponse> findByOrder(UUID orderId);
    List<StockMovementResponse> findByType(String type);
    void delete(UUID id);
}

package com.stratiq.backend.inventory.service;

import com.stratiq.backend.inventory.dto.request.BatchCreateRequest;
import com.stratiq.backend.inventory.dto.request.BatchUpdateRequest;
import com.stratiq.backend.inventory.dto.response.BatchResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface BatchService {
    List<BatchResponse> findAll();
    BatchResponse findById(UUID id);
    List<BatchResponse> findByItem(UUID itemId);
    List<BatchResponse> findByWarehouse(UUID warehouseId);
    BigDecimal currentStock(UUID batchId);
    BatchResponse create(BatchCreateRequest req);
    BatchResponse update(UUID id, BatchUpdateRequest req);
    void delete(UUID id);
}

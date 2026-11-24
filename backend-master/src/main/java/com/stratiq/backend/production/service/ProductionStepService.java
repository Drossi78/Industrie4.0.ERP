package com.stratiq.backend.production.service;

import com.stratiq.backend.production.dto.request.ProductionStepCreateRequest;
import com.stratiq.backend.production.dto.request.ProductionStepUpdateRequest;
import com.stratiq.backend.production.dto.response.ProductionStepResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ProductionStepService {
    ProductionStepResponse create(ProductionStepCreateRequest req);
    ProductionStepResponse update(UUID id, ProductionStepUpdateRequest req);
    ProductionStepResponse get(UUID id);
    Page<ProductionStepResponse> list(Pageable pageable);
    List<ProductionStepResponse> findByItem(UUID itemId);
    void delete(UUID id);
}

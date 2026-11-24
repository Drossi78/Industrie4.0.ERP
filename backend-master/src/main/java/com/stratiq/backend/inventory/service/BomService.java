package com.stratiq.backend.inventory.service;

import com.stratiq.backend.inventory.dto.request.BomCreateRequest;
import com.stratiq.backend.inventory.dto.response.BomResponse;

import java.util.List;
import java.util.UUID;

public interface BomService {
    List<BomResponse> findByItem(UUID itemId);
    BomResponse findById(UUID id);
    BomResponse addComponent(BomCreateRequest req);
    void delete(UUID id);
}

package com.stratiq.backend.inventory.service;

import com.stratiq.backend.inventory.dto.request.ItemCreateRequest;
import com.stratiq.backend.inventory.dto.request.ItemUpdateRequest;
import com.stratiq.backend.inventory.dto.response.ItemResponse;
import com.stratiq.backend.inventory.model.ItemKind;

import java.util.List;
import java.util.UUID;

public interface ItemService {
    List<ItemResponse> findAll();
    ItemResponse findById(UUID id);
    List<ItemResponse> findByCategory(UUID categoryId);
    List<ItemResponse> findByKind(ItemKind kind);
    ItemResponse create(ItemCreateRequest req);
    ItemResponse update(UUID id, ItemUpdateRequest req);
    void delete(UUID id);
}

package com.stratiq.backend.warehouse.service;

import com.stratiq.backend.warehouse.dto.request.WarehouseCreateRequest;
import com.stratiq.backend.warehouse.dto.request.WarehouseUpdateRequest;
import com.stratiq.backend.warehouse.dto.response.WarehouseResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface WarehouseService {
    WarehouseResponse create(WarehouseCreateRequest req);
    WarehouseResponse update(UUID id, WarehouseUpdateRequest req);
    WarehouseResponse get(UUID id);
    Page<WarehouseResponse> list(Pageable pageable);
    List<WarehouseResponse> findByCity(String city);
    List<WarehouseResponse> findActive();
    void delete(UUID id);
}

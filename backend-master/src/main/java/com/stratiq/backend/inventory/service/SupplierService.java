package com.stratiq.backend.inventory.service;

import com.stratiq.backend.inventory.dto.request.SupplierCreateRequest;
import com.stratiq.backend.inventory.dto.request.SupplierUpdateRequest;
import com.stratiq.backend.inventory.dto.response.SupplierResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface SupplierService {
    SupplierResponse create(SupplierCreateRequest req);
    SupplierResponse update(UUID id, SupplierUpdateRequest req);
    SupplierResponse get(UUID id);
    Page<SupplierResponse> list(Pageable pageable);
    List<SupplierResponse> search(String name);
    void delete(UUID id);
}

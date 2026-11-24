package com.stratiq.backend.inventory.service;

import com.stratiq.backend.inventory.dto.request.CategoryCreateRequest;
import com.stratiq.backend.inventory.dto.response.CategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CategoryService {
    CategoryResponse create(CategoryCreateRequest req);
    CategoryResponse get(UUID id);
    Page<CategoryResponse> list(Pageable pageable);
    void delete(UUID id);
}

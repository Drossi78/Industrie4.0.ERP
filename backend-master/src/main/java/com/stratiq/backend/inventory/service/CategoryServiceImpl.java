package com.stratiq.backend.inventory.service;

import com.stratiq.backend.inventory.dto.request.CategoryCreateRequest;
import com.stratiq.backend.inventory.dto.response.CategoryResponse;
import com.stratiq.backend.shared.exception.NotFoundException;
import com.stratiq.backend.inventory.mapper.CategoryMapper;
import com.stratiq.backend.inventory.model.Category;
import com.stratiq.backend.inventory.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repo;
    private final CategoryMapper mapper;

    @Override
    public CategoryResponse create(CategoryCreateRequest req) {
        return mapper.toResponse(repo.save(mapper.toEntity(req)));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse get(UUID id) {
        return mapper.toResponse(find(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> list(Pageable pageable) {
        return repo.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    public void delete(UUID id) {
        if (!repo.existsById(id)) throw new NotFoundException("Category", id);
        repo.deleteById(id);
    }

    private Category find(UUID id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Category", id));
    }
}

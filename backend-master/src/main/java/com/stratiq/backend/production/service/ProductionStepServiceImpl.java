package com.stratiq.backend.production.service;

import com.stratiq.backend.production.dto.request.ProductionStepCreateRequest;
import com.stratiq.backend.production.dto.request.ProductionStepUpdateRequest;
import com.stratiq.backend.production.dto.response.ProductionStepResponse;
import com.stratiq.backend.shared.exception.NotFoundException;
import com.stratiq.backend.production.mapper.ProductionStepMapper;
import com.stratiq.backend.inventory.model.Item;
import com.stratiq.backend.production.model.ProductionStep;
import com.stratiq.backend.inventory.repository.ItemRepository;
import com.stratiq.backend.production.repository.ProductionStepRepository;
import com.stratiq.backend.production.service.ProductionStepService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductionStepServiceImpl implements ProductionStepService {

    private final ProductionStepRepository repo;
    private final ItemRepository itemRepo;
    private final ProductionStepMapper mapper;

    @Override
    public ProductionStepResponse create(ProductionStepCreateRequest req) {
        Item item = itemRepo.findById(req.itemId())
                .orElseThrow(() -> new NotFoundException("Item", req.itemId()));
        return mapper.toResponse(repo.save(mapper.toEntity(req, item)));
    }

    @Override
    public ProductionStepResponse update(UUID id, ProductionStepUpdateRequest req) {
        ProductionStep ps = find(id);
        mapper.update(ps, req);
        return mapper.toResponse(ps);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductionStepResponse get(UUID id) {
        return mapper.toResponse(find(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductionStepResponse> list(Pageable pageable) {
        return repo.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductionStepResponse> findByItem(UUID itemId) {
        // Returns steps ordered by stepOrder (ascending) to respect sequence.
        return repo.findByItemIdAndIsDeletedFalseOrderByStepOrderAsc(itemId)
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    public void delete(UUID id) {
        if (!repo.existsById(id)) throw new NotFoundException("ProductionStep", id);
        repo.deleteById(id);
    }

    private ProductionStep find(UUID id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("ProductionStep", id));
    }
}

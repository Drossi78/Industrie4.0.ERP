package com.stratiq.backend.inventory.service;

import com.stratiq.backend.inventory.dto.request.ItemCreateRequest;
import com.stratiq.backend.inventory.dto.request.ItemUpdateRequest;
import com.stratiq.backend.inventory.dto.response.ItemResponse;
import com.stratiq.backend.shared.exception.NotFoundException;
import com.stratiq.backend.inventory.mapper.ItemMapper;
import com.stratiq.backend.inventory.model.Category;
import com.stratiq.backend.inventory.model.Item;
import com.stratiq.backend.inventory.model.ItemKind;
import com.stratiq.backend.inventory.repository.CategoryRepository;
import com.stratiq.backend.inventory.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemServiceImpl implements ItemService {

    private final ItemRepository repo;
    private final CategoryRepository categoryRepo;
    private final ItemMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> findAll() {
        // Soft-delete filter: excludes items marked as deleted (is_deleted=true).
        return repo.findByIsDeletedFalse().stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ItemResponse findById(UUID id) {
        return mapper.toResponse(find(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> findByCategory(UUID categoryId) {
        return repo.findByCategoryId(categoryId).stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> findByKind(ItemKind kind) {
        return repo.findByKind(kind).stream().map(mapper::toResponse).toList();
    }

    @Override
    public ItemResponse create(ItemCreateRequest req) {
        Item item = new Item();
        item.setName(req.name());
        item.setKind(req.kind() != null ? req.kind() : ItemKind.OWN_PRODUCT);
        item.setUnit(req.unit());
        item.setPrice(req.price());
        item.setDescription(req.description());
        item.setNotes(req.notes());
        item.setDeleted(false);
        if (req.categoryId() != null) {
            Category cat = categoryRepo.findById(req.categoryId())
                .orElseThrow(() -> new NotFoundException("Category", req.categoryId()));
            item.setCategory(cat);
        }
        return mapper.toResponse(repo.save(item));
    }

    @Override
    public ItemResponse update(UUID id, ItemUpdateRequest req) {
        Item item = find(id);
        if (req.name() != null) item.setName(req.name());
        if (req.kind() != null) item.setKind(req.kind());
        if (req.unit() != null) item.setUnit(req.unit());
        if (req.price() != null) item.setPrice(req.price());
        if (req.description() != null) item.setDescription(req.description());
        if (req.notes() != null) item.setNotes(req.notes());
        if (req.categoryId() != null) {
            Category cat = categoryRepo.findById(req.categoryId())
                .orElseThrow(() -> new NotFoundException("Category", req.categoryId()));
            item.setCategory(cat);
        }
        return mapper.toResponse(repo.save(item));
    }

    @Override
    public void delete(UUID id) {
        Item item = find(id);
        item.setDeleted(true);
        repo.save(item);
    }

    private Item find(UUID id) {
        return repo.findByIdAndIsDeletedFalse(id)
            .orElseThrow(() -> new NotFoundException("Item", id));
    }
}

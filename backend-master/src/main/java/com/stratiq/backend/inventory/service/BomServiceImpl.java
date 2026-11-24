package com.stratiq.backend.inventory.service;

import com.stratiq.backend.inventory.dto.request.BomCreateRequest;
import com.stratiq.backend.inventory.dto.response.BomResponse;
import com.stratiq.backend.shared.exception.NotFoundException;
import com.stratiq.backend.inventory.mapper.BomMapper;
import com.stratiq.backend.inventory.model.BillOfMaterial;
import com.stratiq.backend.inventory.model.Item;
import com.stratiq.backend.inventory.model.ItemKind;
import com.stratiq.backend.inventory.repository.BillOfMaterialRepository;
import com.stratiq.backend.inventory.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BomServiceImpl implements BomService {

    private final BillOfMaterialRepository repo;
    private final ItemRepository itemRepo;
    private final BomMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<BomResponse> findByItem(UUID itemId) {
        return repo.findByItemId(itemId).stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BomResponse findById(UUID id) {
        return mapper.toResponse(find(id));
    }

    @Override
    public BomResponse addComponent(BomCreateRequest req) {
        // Links a component item to a parent item with quantity. Single-level BOM only:
        // the parent must be manufactured (OWN_PRODUCT), the component must be stocked-as-is
        // (PURCHASED) — no nested/sub-assembly BOMs.
        Item item = resolveItem(req.itemId());
        Item component = resolveItem(req.componentId());
        if (item.getKind() != ItemKind.OWN_PRODUCT) {
            throw new IllegalArgumentException("BOM parent item must be of kind OWN_PRODUCT: " + item.getId());
        }
        if (component.getKind() != ItemKind.PURCHASED) {
            throw new IllegalArgumentException("BOM component item must be of kind PURCHASED: " + component.getId());
        }

        BillOfMaterial bom = new BillOfMaterial();
        bom.setItem(item);
        bom.setComponent(component);
        bom.setQuantity(req.quantity());
        bom.setUnit(req.unit());
        bom.setNotes(req.notes());
        return mapper.toResponse(repo.save(bom));
    }

    @Override
    public void delete(UUID id) {
        if (!repo.existsById(id)) throw new NotFoundException("BillOfMaterial", id);
        repo.deleteById(id);
    }

    private BillOfMaterial find(UUID id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("BillOfMaterial", id));
    }

    private Item resolveItem(UUID id) {
        return itemRepo.findById(id).orElseThrow(() -> new NotFoundException("Item", id));
    }
}

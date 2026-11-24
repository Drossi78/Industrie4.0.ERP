package com.stratiq.backend.inventory.service;

import com.stratiq.backend.inventory.dto.request.BatchCreateRequest;
import com.stratiq.backend.inventory.dto.request.BatchUpdateRequest;
import com.stratiq.backend.inventory.dto.response.BatchResponse;
import com.stratiq.backend.shared.exception.NotFoundException;
import com.stratiq.backend.inventory.mapper.BatchMapper;
import com.stratiq.backend.inventory.model.Batch;
import com.stratiq.backend.inventory.model.Item;
import com.stratiq.backend.warehouse.model.Warehouse;
import com.stratiq.backend.inventory.repository.BatchRepository;
import com.stratiq.backend.inventory.repository.ItemRepository;
import com.stratiq.backend.warehouse.dto.internal.BatchIdBalance;
import com.stratiq.backend.warehouse.repository.StockMovementRepository;
import com.stratiq.backend.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BatchServiceImpl implements BatchService {

    private final BatchRepository repo;
    private final ItemRepository itemRepo;
    private final WarehouseRepository warehouseRepo;
    // Cross-domain: batch stock is derived from the warehouse's movement ledger, not stored.
    private final StockMovementRepository movementRepo;
    private final BatchMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<BatchResponse> findAll() {
        return toResponses(repo.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public BatchResponse findById(UUID id) {
        Batch batch = find(id);
        return mapper.toResponse(batch, BigDecimal.valueOf(movementRepo.findBatchBalance(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BatchResponse> findByItem(UUID itemId) {
        return toResponses(repo.findByItemId(itemId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BatchResponse> findByWarehouse(UUID warehouseId) {
        return toResponses(repo.findByWarehouseId(warehouseId));
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal currentStock(UUID batchId) {
        find(batchId); // 404 if the batch does not exist
        return BigDecimal.valueOf(movementRepo.findBatchBalance(batchId));
    }

    @Override
    public BatchResponse create(BatchCreateRequest req) {
        Batch batch = new Batch();
        batch.setBatchNumber(req.batchNumber());
        batch.setExpiryDate(req.expiryDate());
        // Cross-domain validation: resolves Item and Warehouse references.
        batch.setItem(resolveItem(req.itemId()));
        batch.setWarehouse(resolveWarehouse(req.warehouseId()));
        // A fresh batch has no movements yet, so its derived stock is zero.
        return mapper.toResponse(repo.save(batch), BigDecimal.ZERO);
    }

    @Override
    public BatchResponse update(UUID id, BatchUpdateRequest req) {
        Batch batch = find(id);
        if (req.batchNumber() != null) batch.setBatchNumber(req.batchNumber());
        if (req.expiryDate() != null) batch.setExpiryDate(req.expiryDate());
        if (req.itemId() != null) batch.setItem(resolveItem(req.itemId()));
        if (req.warehouseId() != null) batch.setWarehouse(resolveWarehouse(req.warehouseId()));
        Batch saved = repo.save(batch);
        return mapper.toResponse(saved, BigDecimal.valueOf(movementRepo.findBatchBalance(saved.getId())));
    }

    @Override
    public void delete(UUID id) {
        if (!repo.existsById(id)) throw new NotFoundException("Batch", id);
        repo.deleteById(id);
    }

    // Maps a list of batches to responses, enriching each with its derived stock in a single query.
    private List<BatchResponse> toResponses(List<Batch> batches) {
        if (batches.isEmpty()) return List.of();
        List<UUID> ids = batches.stream().map(Batch::getId).toList();
        Map<UUID, Long> balances = movementRepo.findBalancesForBatches(ids).stream()
            .collect(Collectors.toMap(BatchIdBalance::batchId, BatchIdBalance::balance));
        return batches.stream()
            .map(b -> mapper.toResponse(b, BigDecimal.valueOf(balances.getOrDefault(b.getId(), 0L))))
            .toList();
    }

    private Batch find(UUID id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Batch", id));
    }

    private Item resolveItem(UUID id) {
        return itemRepo.findById(id).orElseThrow(() -> new NotFoundException("Item", id));
    }

    private Warehouse resolveWarehouse(UUID id) {
        return warehouseRepo.findById(id).orElseThrow(() -> new NotFoundException("Warehouse", id));
    }
}

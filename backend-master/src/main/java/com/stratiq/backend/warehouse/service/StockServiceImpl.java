package com.stratiq.backend.warehouse.service;

import com.stratiq.backend.warehouse.dto.request.AdjustmentRequest;
import com.stratiq.backend.warehouse.dto.request.GoodsIssueRequest;
import com.stratiq.backend.warehouse.dto.request.GoodsReceiptRequest;
import com.stratiq.backend.warehouse.dto.request.StockTransferRequest;
import com.stratiq.backend.warehouse.dto.response.DifferenceReportResponse;
import com.stratiq.backend.warehouse.dto.response.StockLevelResponse;
import com.stratiq.backend.warehouse.dto.response.StockMovementResponse;
import com.stratiq.backend.warehouse.dto.response.WarehouseStockResponse;
import com.stratiq.backend.shared.exception.InsufficientStockException;
import com.stratiq.backend.shared.exception.NotFoundException;
import com.stratiq.backend.warehouse.dto.internal.BatchBalance;
import com.stratiq.backend.warehouse.dto.response.BatchStockResponse;
import com.stratiq.backend.warehouse.mapper.StockMovementMapper;
import com.stratiq.backend.inventory.model.Batch;
import com.stratiq.backend.inventory.model.Item;
import com.stratiq.backend.order.model.Order;
import com.stratiq.backend.warehouse.model.StockMovement;
import com.stratiq.backend.warehouse.model.Warehouse;
import com.stratiq.backend.inventory.repository.BatchRepository;
import com.stratiq.backend.inventory.repository.ItemRepository;
import com.stratiq.backend.order.repository.OrderRepository;
import com.stratiq.backend.warehouse.repository.StockMovementRepository;
import com.stratiq.backend.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class StockServiceImpl implements StockService {

    private final StockMovementRepository repo;
    private final ItemRepository itemRepo;
    private final WarehouseRepository warehouseRepo;
    private final OrderRepository orderRepo;
    private final BatchRepository batchRepo;
    private final StockMovementMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Page<StockMovementResponse> findAll(Pageable pageable) {
        return repo.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    public StockMovementResponse adjustment(AdjustmentRequest req) {
        Item item = req.itemId() != null
            ? itemRepo.findById(req.itemId()).orElseThrow(() -> new NotFoundException("Item", req.itemId()))
            : null;
        Warehouse warehouse = req.warehouseId() != null
            ? warehouseRepo.findById(req.warehouseId()).orElseThrow(() -> new NotFoundException("Warehouse", req.warehouseId()))
            : null;

        StockMovement movement = new StockMovement();
        movement.setQuantity(req.quantity() != null ? Math.abs(req.quantity()) : 0L);
        movement.setNote(req.note());
        // IN/OUT determined by quantity sign: negative = OUT, positive = IN.
        movement.setType(req.quantity() != null && req.quantity() < 0 ? "OUT" : "IN");
        movement.setItem(item);
        movement.setWarehouse(warehouse);
        return mapper.toResponse(repo.save(movement));
    }

    @Override
    @Transactional(readOnly = true)
    public DifferenceReportResponse differenceReport(UUID itemId, UUID warehouseId) {
        List<StockMovement> movements = repo.findByItemId(itemId);
        if (warehouseId != null) {
            movements = movements.stream()
                .filter(m -> m.getWarehouse() != null && m.getWarehouse().getId().equals(warehouseId))
                .toList();
        }

        // Supports both English (IN/OUT) and legacy German (EINGANG/AUSGANG) terminology.
        long totalIn = movements.stream()
            .filter(m -> "IN".equalsIgnoreCase(m.getType()) || "EINGANG".equalsIgnoreCase(m.getType()))
            .mapToLong(m -> m.getQuantity() != null ? m.getQuantity() : 0L)
            .sum();
        long totalOut = movements.stream()
            .filter(m -> "OUT".equalsIgnoreCase(m.getType()) || "AUSGANG".equalsIgnoreCase(m.getType()))
            .mapToLong(m -> m.getQuantity() != null ? m.getQuantity() : 0L)
            .sum();
        long balance = totalIn - totalOut;

        Item item = itemId != null
            ? itemRepo.findById(itemId).orElse(null)
            : null;
        Warehouse warehouse = warehouseId != null
            ? warehouseRepo.findById(warehouseId).orElse(null)
            : null;

        return new DifferenceReportResponse(
            itemId,
            item != null ? item.getName() : null,
            warehouseId,
            warehouse != null ? warehouse.getName() : null,
            totalIn,
            totalOut,
            balance
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockLevelResponse> getStockLevels() {
        return repo.findStockLevels();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseStockResponse> getStockByWarehouse(UUID itemId) {
        return repo.findStockByWarehouseForItem(itemId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockLevelResponse> getStockLevelsForWarehouse(UUID warehouseId) {
        return repo.findStockLevelsByWarehouse(warehouseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BatchStockResponse> getBatchStock(UUID itemId, UUID warehouseId) {
        return repo.findBatchBalances(itemId, warehouseId).stream()
            .map(bb -> new BatchStockResponse(
                bb.batch().getId(),
                bb.batch().getBatchNumber(),
                bb.batch().getExpiryDate(),
                bb.balance()))
            .toList();
    }

    @Override
    public StockMovementResponse goodsReceipt(GoodsReceiptRequest req) {
        Item item = requireItem(req.itemId());
        Warehouse warehouse = requireWarehouse(req.warehouseId());
        Order order = resolveOrder(req.orderId());
        // Optional batch tracking: book the receipt onto a lot, creating it on first use.
        Batch batch = req.batchNumber() != null && !req.batchNumber().isBlank()
            ? resolveOrCreateBatch(item, warehouse, req.batchNumber().trim(), req.expiryDate())
            : null;
        StockMovement movement = saveMovement(item, warehouse, order, batch, req.quantity(), "IN", req.note());
        return mapper.toResponse(movement);
    }

    @Override
    public List<StockMovementResponse> goodsIssue(GoodsIssueRequest req) {
        Item item = requireItem(req.itemId());
        Warehouse warehouse = requireWarehouse(req.warehouseId());
        Order order = resolveOrder(req.orderId());

        // Manual selection: take the whole quantity from one explicitly chosen batch.
        if (req.batchId() != null) {
            Batch batch = batchRepo.findById(req.batchId())
                .orElseThrow(() -> new NotFoundException("Batch", req.batchId()));
            long available = repo.findBatchBalance(batch.getId());
            if (available < req.quantity()) {
                throw new InsufficientStockException(
                    "Insufficient stock in batch %s: available %d, requested %d"
                        .formatted(batch.getBatchNumber(), available, req.quantity()));
            }
            StockMovement m = saveMovement(item, warehouse, order, batch, req.quantity(), "OUT", req.note());
            return List.of(mapper.toResponse(m));
        }

        // Automatic allocation across batches with remaining stock (FEFO, then FIFO).
        List<BatchBalance> batches = repo.findBatchBalances(req.itemId(), req.warehouseId());
        if (batches.isEmpty()) {
            // No batch tracking for this item/warehouse: fall back to a single untracked movement.
            requireSufficientStock(req.itemId(), req.warehouseId(), req.quantity());
            StockMovement m = saveMovement(item, warehouse, order, null, req.quantity(), "OUT", req.note());
            return List.of(mapper.toResponse(m));
        }

        long totalAvailable = batches.stream().mapToLong(BatchBalance::balance).sum();
        if (totalAvailable < req.quantity()) {
            throw new InsufficientStockException(
                "Insufficient stock: available %d, requested %d".formatted(totalAvailable, req.quantity()));
        }

        // Consume batches in order until the requested quantity is covered; one OUT per lot touched.
        List<StockMovementResponse> movements = new ArrayList<>();
        long remaining = req.quantity();
        for (BatchBalance bb : batches) {
            if (remaining <= 0) break;
            long take = Math.min(bb.balance(), remaining);
            StockMovement m = saveMovement(item, warehouse, order, bb.batch(), take, "OUT", req.note());
            movements.add(mapper.toResponse(m));
            remaining -= take;
        }
        return movements;
    }

    @Override
    public List<StockMovementResponse> transfer(StockTransferRequest req) {
        if (req.fromWarehouseId().equals(req.toWarehouseId())) {
            throw new IllegalArgumentException("Source and target warehouse must differ");
        }
        Item item = requireItem(req.itemId());
        Warehouse from = requireWarehouse(req.fromWarehouseId());
        Warehouse to = requireWarehouse(req.toWarehouseId());
        requireSufficientStock(req.itemId(), req.fromWarehouseId(), req.quantity());

        // A transfer is modeled as two movements so per-warehouse balances stay correct:
        // an OUT in the source warehouse and an IN in the target warehouse.
        String suffix = req.note() != null && !req.note().isBlank() ? " - " + req.note() : "";
        StockMovement out = saveMovement(item, from, null, null, req.quantity(), "OUT",
                "Transfer to " + to.getName() + suffix);
        StockMovement in = saveMovement(item, to, null, null, req.quantity(), "IN",
                "Transfer from " + from.getName() + suffix);
        return List.of(mapper.toResponse(out), mapper.toResponse(in));
    }

    // Throws InsufficientStockException if the available balance in the warehouse is below quantity.
    private void requireSufficientStock(UUID itemId, UUID warehouseId, long quantity) {
        long available = differenceReport(itemId, warehouseId).balance();
        if (available < quantity) {
            throw new InsufficientStockException(
                    "Insufficient stock: available %d, requested %d".formatted(available, quantity));
        }
    }

    // Creates and persists a stock movement. Quantity is always stored positive; direction is the type.
    // Batch is optional (null = untracked stock).
    private StockMovement saveMovement(Item item, Warehouse warehouse, Order order, Batch batch,
                                       long quantity, String type, String note) {
        StockMovement m = new StockMovement();
        m.setItem(item);
        m.setWarehouse(warehouse);
        m.setOrder(order);
        m.setBatch(batch);
        m.setQuantity(quantity);
        m.setType(type);
        m.setNote(note);
        return repo.save(m);
    }

    // Finds an existing lot by number within the item+warehouse, or creates a new one.
    // The batch is metadata only; its stock balance is derived from the movements, not stored here.
    private Batch resolveOrCreateBatch(Item item, Warehouse warehouse, String batchNumber,
                                       java.time.LocalDateTime expiryDate) {
        return batchRepo
            .findByBatchNumberAndItemIdAndWarehouseId(batchNumber, item.getId(), warehouse.getId())
            .orElseGet(() -> {
                Batch b = new Batch();
                b.setBatchNumber(batchNumber);
                b.setItem(item);
                b.setWarehouse(warehouse);
                b.setExpiryDate(expiryDate);
                return batchRepo.save(b);
            });
    }

    private Item requireItem(UUID id) {
        return itemRepo.findById(id).orElseThrow(() -> new NotFoundException("Item", id));
    }

    private Warehouse requireWarehouse(UUID id) {
        return warehouseRepo.findById(id).orElseThrow(() -> new NotFoundException("Warehouse", id));
    }

    private Order resolveOrder(UUID id) {
        if (id == null) return null;
        return orderRepo.findById(id).orElseThrow(() -> new NotFoundException("Order", id));
    }
}

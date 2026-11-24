package com.stratiq.backend.warehouse.service;

import com.stratiq.backend.warehouse.dto.request.StockMovementCreateRequest;
import com.stratiq.backend.warehouse.dto.request.StockMovementUpdateRequest;
import com.stratiq.backend.warehouse.dto.response.StockMovementResponse;
import com.stratiq.backend.shared.exception.NotFoundException;
import com.stratiq.backend.warehouse.mapper.StockMovementMapper;
import com.stratiq.backend.inventory.model.Item;
import com.stratiq.backend.order.model.Order;
import com.stratiq.backend.warehouse.model.StockMovement;
import com.stratiq.backend.warehouse.model.Warehouse;
import com.stratiq.backend.inventory.repository.ItemRepository;
import com.stratiq.backend.order.repository.OrderRepository;
import com.stratiq.backend.warehouse.repository.StockMovementRepository;
import com.stratiq.backend.warehouse.repository.WarehouseRepository;
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
public class StockMovementServiceImpl implements StockMovementService {

    private final StockMovementRepository repo;
    private final ItemRepository itemRepo;
    private final OrderRepository orderRepo;
    private final WarehouseRepository warehouseRepo;
    private final StockMovementMapper mapper;

    @Override
    public StockMovementResponse create(StockMovementCreateRequest req) {
        Item item = resolveItem(req.itemId());
        Order order = resolveOrder(req.orderId());
        Warehouse warehouse = resolveWarehouse(req.warehouseId());
        return mapper.toResponse(repo.save(mapper.toEntity(req, item, order, warehouse)));
    }

    @Override
    public StockMovementResponse update(UUID id, StockMovementUpdateRequest req) {
        StockMovement m = find(id);
        Item item = req.itemId() != null ? resolveItem(req.itemId()) : null;
        Order order = req.orderId() != null ? resolveOrder(req.orderId()) : null;
        Warehouse warehouse = req.warehouseId() != null ? resolveWarehouse(req.warehouseId()) : null;
        mapper.update(m, req, item, order, warehouse);
        return mapper.toResponse(m);
    }

    @Override
    @Transactional(readOnly = true)
    public StockMovementResponse get(UUID id) {
        return mapper.toResponse(find(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StockMovementResponse> list(Pageable pageable) {
        return repo.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMovementResponse> findByItem(UUID itemId) {
        return repo.findByItemId(itemId).stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMovementResponse> findByWarehouse(UUID warehouseId) {
        return repo.findByWarehouseId(warehouseId).stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMovementResponse> findByOrder(UUID orderId) {
        return repo.findByOrderId(orderId).stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMovementResponse> findByType(String type) {
        return repo.findByType(type).stream().map(mapper::toResponse).toList();
    }

    @Override
    public void delete(UUID id) {
        if (!repo.existsById(id)) throw new NotFoundException("StockMovement", id);
        repo.deleteById(id);
    }

    private StockMovement find(UUID id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("StockMovement", id));
    }

    private Item resolveItem(UUID id) {
        if (id == null) return null;
        return itemRepo.findById(id).orElseThrow(() -> new NotFoundException("Item", id));
    }

    private Order resolveOrder(UUID id) {
        if (id == null) return null;
        return orderRepo.findById(id).orElseThrow(() -> new NotFoundException("Order", id));
    }

    private Warehouse resolveWarehouse(UUID id) {
        if (id == null) return null;
        return warehouseRepo.findById(id).orElseThrow(() -> new NotFoundException("Warehouse", id));
    }
}

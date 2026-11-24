package com.stratiq.backend.order.service;

import com.stratiq.backend.order.dto.request.OrderItemCreateRequest;
import com.stratiq.backend.order.dto.request.OrderItemUpdateRequest;
import com.stratiq.backend.order.dto.response.OrderItemResponse;
import com.stratiq.backend.shared.exception.NotFoundException;
import com.stratiq.backend.order.mapper.OrderItemMapper;
import com.stratiq.backend.inventory.model.Item;
import com.stratiq.backend.order.model.Order;
import com.stratiq.backend.order.model.OrderItem;
import com.stratiq.backend.inventory.model.Supplier;
import com.stratiq.backend.inventory.repository.ItemRepository;
import com.stratiq.backend.order.repository.OrderItemRepository;
import com.stratiq.backend.order.repository.OrderRepository;
import com.stratiq.backend.inventory.repository.SupplierRepository;
import com.stratiq.backend.order.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderItemRepository repo;
    private final OrderRepository orderRepo;
    private final ItemRepository itemRepo;
    private final SupplierRepository supplierRepo;
    private final OrderItemMapper mapper;

    @Override
    public OrderItemResponse create(OrderItemCreateRequest req) {
        // Validates Order (required) and Item (required). Supplier is optional for purchased items.
        Order order = orderRepo.findById(req.orderId())
                .orElseThrow(() -> new NotFoundException("Order", req.orderId()));
        Item item = itemRepo.findById(req.itemId())
                .orElseThrow(() -> new NotFoundException("Item", req.itemId()));
        Supplier supplier = req.supplierId() != null
                ? supplierRepo.findById(req.supplierId())
                        .orElseThrow(() -> new NotFoundException("Supplier", req.supplierId()))
                : null;
        OrderItemResponse response = mapper.toResponse(repo.save(mapper.toEntity(req, order, item, supplier)));
        recalcOrderTotal(order.getId());
        return response;
    }

    @Override
    public OrderItemResponse update(UUID id, OrderItemUpdateRequest req) {
        OrderItem oi = find(id);
        Supplier supplier = req.supplierId() != null
                ? supplierRepo.findById(req.supplierId())
                        .orElseThrow(() -> new NotFoundException("Supplier", req.supplierId()))
                : null;
        mapper.update(oi, req, supplier);
        OrderItemResponse response = mapper.toResponse(oi);
        recalcOrderTotal(oi.getOrder().getId());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderItemResponse get(UUID id) {
        return mapper.toResponse(find(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderItemResponse> list(Pageable pageable) {
        return repo.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItemResponse> findByOrder(UUID orderId) {
        return repo.findByOrderId(orderId).stream().map(mapper::toResponse).toList();
    }

    @Override
    public void delete(UUID id) {
        OrderItem oi = find(id);
        UUID orderId = oi.getOrder().getId();
        repo.delete(oi);
        recalcOrderTotal(orderId);
    }

    // Recomputes the order's total from its line items. Kept in sync on every item add/update/delete
    // so the order's displayed amount matches its positions.
    private void recalcOrderTotal(UUID orderId) {
        BigDecimal total = repo.findByOrderId(orderId).stream()
                .map(OrderItem::getLineTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        orderRepo.findById(orderId).ifPresent(o -> {
            o.setTotalAmount(total);
            orderRepo.save(o);
        });
    }

    private OrderItem find(UUID id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("OrderItem", id));
    }
}

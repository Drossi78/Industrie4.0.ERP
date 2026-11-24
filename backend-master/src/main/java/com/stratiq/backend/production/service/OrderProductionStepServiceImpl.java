package com.stratiq.backend.production.service;

import com.stratiq.backend.inventory.model.BillOfMaterial;
import com.stratiq.backend.inventory.repository.BillOfMaterialRepository;
import com.stratiq.backend.production.dto.request.OrderProductionStepCreateRequest;
import com.stratiq.backend.production.dto.request.OrderProductionStepUpdateRequest;
import com.stratiq.backend.production.dto.request.ProductionStepPatchRequest;
import com.stratiq.backend.production.dto.response.OrderProductionStepResponse;
import com.stratiq.backend.shared.exception.NotFoundException;
import com.stratiq.backend.production.mapper.OrderProductionStepMapper;
import com.stratiq.backend.order.model.Order;
import com.stratiq.backend.order.model.OrderItem;
import com.stratiq.backend.order.model.OrderStatus;
import com.stratiq.backend.production.model.OrderProductionStep;
import com.stratiq.backend.production.model.ProductionStep;
import com.stratiq.backend.production.model.ProductionStepStatus;
import com.stratiq.backend.order.repository.OrderItemRepository;
import com.stratiq.backend.production.repository.OrderProductionStepRepository;
import com.stratiq.backend.order.repository.OrderRepository;
import com.stratiq.backend.production.repository.ProductionStepRepository;
import com.stratiq.backend.production.service.OrderProductionStepService;
import com.stratiq.backend.warehouse.dto.request.GoodsIssueRequest;
import com.stratiq.backend.warehouse.dto.request.GoodsReceiptRequest;
import com.stratiq.backend.warehouse.model.ReservationStatus;
import com.stratiq.backend.warehouse.model.StockReservation;
import com.stratiq.backend.warehouse.repository.StockReservationRepository;
import com.stratiq.backend.warehouse.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderProductionStepServiceImpl implements OrderProductionStepService {

    private final OrderProductionStepRepository repo;
    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final ProductionStepRepository productionStepRepo;
    private final BillOfMaterialRepository bomRepo;
    private final StockReservationRepository reservationRepo;
    private final StockService stockService;
    private final OrderProductionStepMapper mapper;

    @Override
    public OrderProductionStepResponse create(OrderProductionStepCreateRequest req) {
        // Bridges Order domain (Order, OrderItem) with Production domain (ProductionStep template).
        // Validates all related entities exist before creating the assignment.
        Order order = orderRepo.findById(req.orderId())
                .orElseThrow(() -> new NotFoundException("Order", req.orderId()));
        OrderItem orderItem = orderItemRepo.findById(req.orderItemId())
                .orElseThrow(() -> new NotFoundException("OrderItem", req.orderItemId()));
        ProductionStep templateStep = productionStepRepo.findById(req.templateStepId())
                .orElseThrow(() -> new NotFoundException("ProductionStep", req.templateStepId()));
        return mapper.toResponse(repo.save(mapper.toEntity(req, order, orderItem, templateStep)));
    }

    @Override
    public OrderProductionStepResponse update(UUID id, OrderProductionStepUpdateRequest req) {
        OrderProductionStep ops = find(id);
        mapper.update(ops, req);
        return mapper.toResponse(ops);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderProductionStepResponse get(UUID id) {
        return mapper.toResponse(find(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderProductionStepResponse> list(Pageable pageable) {
        return repo.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderProductionStepResponse> findByOrder(UUID orderId) {
        return repo.findByOrderIdOrderByStepOrderAsc(orderId)
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderProductionStepResponse> findByOrderItem(UUID orderItemId) {
        return repo.findByOrderItemIdOrderByStepOrderAsc(orderItemId)
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    public OrderProductionStepResponse patch(UUID id, ProductionStepPatchRequest req) {
        OrderProductionStep step = find(id);
        if (step.getOrder().getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot change production steps of a cancelled order: " + step.getOrder().getId());
        }
        if (req.getStatus() != null) {
            ProductionStepStatus newStatus = req.getStatus();
            step.setStatus(newStatus);
            switch (newStatus) {
                case IN_PROGRESS -> { if (step.getStartedAt() == null) step.setStartedAt(java.time.LocalDateTime.now()); }
                case DONE        -> step.setCompletedAt(java.time.LocalDateTime.now());
                case PENDING     -> { step.setStartedAt(null); step.setCompletedAt(null); }
            }
        }
        if (req.isAssignedToProvided()) {
            step.setAssignedTo(req.getAssignedTo());
        }
        OrderProductionStep saved = repo.save(step);

        if (req.getStatus() == ProductionStepStatus.DONE && isLastStepDone(saved.getOrderItem().getId())) {
            completeProduction(saved, req.getWarehouseId());
        }
        return mapper.toResponse(saved);
    }

    private boolean isLastStepDone(UUID orderItemId) {
        return repo.findByOrderItemIdOrderByStepOrderAsc(orderItemId).stream()
                .allMatch(s -> s.getStatus() == ProductionStepStatus.DONE);
    }

    // Books the finished good into stock and converts this order line's BOM reservations
    // into an actual stock-out. Runs once, when the last production step of an order line completes.
    private void completeProduction(OrderProductionStep lastStep, UUID warehouseId) {
        if (warehouseId == null) {
            throw new IllegalArgumentException("warehouseId is required to complete the last production step");
        }
        OrderItem orderItem = lastStep.getOrderItem();
        Order order = lastStep.getOrder();
        long qty = orderItem.getQuantity().setScale(0, RoundingMode.HALF_UP).longValueExact();
        String note = "Production completed for order " + (order.getOrderNumber() != null ? order.getOrderNumber() : order.getId());

        // Own production yields a distinct lot: auto-assign a production batch number so the
        // finished goods are booked into (and traceable via) their own batch.
        String batchNumber = productionBatchNumber(order);
        stockService.goodsReceipt(new GoodsReceiptRequest(orderItem.getItem().getId(), warehouseId, qty, batchNumber, null, note, order.getId()));

        for (BillOfMaterial bomLine : bomRepo.findByItemId(orderItem.getItem().getId())) {
            long neededQty = bomLine.getQuantity().multiply(orderItem.getQuantity())
                    .setScale(0, RoundingMode.HALF_UP).longValueExact();
            consumeReservations(bomLine.getComponent().getId(), order.getId(), neededQty, note);
        }
    }

    private static final DateTimeFormatter BATCH_TS = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    // Builds a readable, unique batch number for a completed own production, e.g. "PROD-ORD-001-20260710-143205".
    private String productionBatchNumber(Order order) {
        String orderRef = order.getOrderNumber() != null && !order.getOrderNumber().isBlank()
                ? order.getOrderNumber()
                : order.getId().toString().substring(0, 8);
        return "PROD-" + orderRef + "-" + LocalDateTime.now().format(BATCH_TS);
    }

    // Converts up to neededQty of this order's ACTIVE reservations for the given component item
    // into an actual stock-out, splitting a reservation row if it holds more than needed. Reservations
    // aren't tagged to a specific order line, but that's fine here: converting any neededQty worth of
    // the order's ACTIVE stock for this component still leaves the correct remainder ACTIVE for any
    // other order line in the same order that needs the same component and hasn't finished yet.
    private void consumeReservations(UUID componentId, UUID orderId, long neededQty, String note) {
        long remaining = neededQty;
        for (StockReservation reservation : reservationRepo.findByItemIdAndOrderIdAndStatus(componentId, orderId, ReservationStatus.ACTIVE)) {
            if (remaining <= 0) {
                break;
            }
            long take = Math.min(remaining, reservation.getQuantity());
            stockService.goodsIssue(new GoodsIssueRequest(componentId, reservation.getWarehouse().getId(), take, null, note, orderId));
            if (take == reservation.getQuantity()) {
                reservation.setStatus(ReservationStatus.FULFILLED);
                reservation.setReleasedAt(LocalDateTime.now());
            } else {
                reservation.setQuantity(reservation.getQuantity() - take);
            }
            remaining -= take;
        }
        if (remaining > 0) {
            throw new IllegalStateException(
                    "Not enough active reservation for component %s on order %s: missing %d"
                            .formatted(componentId, orderId, remaining));
        }
    }

    @Override
    public void delete(UUID id) {
        if (!repo.existsById(id)) throw new NotFoundException("OrderProductionStep", id);
        repo.deleteById(id);
    }

    private OrderProductionStep find(UUID id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("OrderProductionStep", id));
    }
}

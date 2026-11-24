package com.stratiq.backend.order.service;

import com.stratiq.backend.inventory.model.BillOfMaterial;
import com.stratiq.backend.inventory.repository.BillOfMaterialRepository;
import com.stratiq.backend.order.dto.request.OrderCreateRequest;
import com.stratiq.backend.order.dto.request.OrderUpdateRequest;
import com.stratiq.backend.order.dto.response.OrderResponse;
import com.stratiq.backend.shared.exception.InsufficientStockException;
import com.stratiq.backend.shared.exception.NotFoundException;
import com.stratiq.backend.order.mapper.OrderMapper;
import com.stratiq.backend.order.model.Customer;
import com.stratiq.backend.order.model.Order;
import com.stratiq.backend.order.model.OrderItem;
import com.stratiq.backend.order.model.OrderDirection;
import com.stratiq.backend.order.model.OrderStatus;
import com.stratiq.backend.order.repository.CustomerRepository;
import com.stratiq.backend.order.repository.OrderItemRepository;
import com.stratiq.backend.order.repository.OrderRepository;
import com.stratiq.backend.order.service.OrderService;
import com.stratiq.backend.warehouse.dto.request.CreateReservationRequest;
import com.stratiq.backend.warehouse.dto.response.WarehouseStockResponse;
import com.stratiq.backend.warehouse.model.ReservationStatus;
import com.stratiq.backend.warehouse.repository.StockMovementRepository;
import com.stratiq.backend.warehouse.repository.StockReservationRepository;
import com.stratiq.backend.warehouse.service.StockReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository repo;
    private final CustomerRepository customerRepo;
    private final OrderItemRepository orderItemRepo;
    private final BillOfMaterialRepository bomRepo;
    private final StockMovementRepository movementRepo;
    private final StockReservationRepository reservationRepo;
    private final StockReservationService reservationService;
    private final OrderMapper mapper;

    @Override
    public OrderResponse create(OrderCreateRequest req) {
        // Validates customer exists before creating order. Throws NotFoundException if missing.
        Customer customer = resolveCustomer(req.customerId());
        return mapper.toResponse(repo.save(mapper.toEntity(req, customer)));
    }

    @Override
    public OrderResponse update(UUID id, OrderUpdateRequest req) {
        Order o = find(id);
        Customer customer = req.customerId() != null ? resolveCustomer(req.customerId()) : null;
        mapper.update(o, req, customer);
        return mapper.toResponse(o);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse get(UUID id) {
        return mapper.toResponse(find(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> list(Pageable pageable) {
        return repo.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findByCustomer(UUID customerId) {
        return repo.findByCustomerId(customerId).stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findByStatus(OrderStatus status) {
        return repo.findByStatus(status).stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findByDirection(OrderDirection direction) {
        return repo.findByDirection(direction).stream().map(mapper::toResponse).toList();
    }

    @Override
    public void delete(UUID id) {
        if (!repo.existsById(id)) throw new NotFoundException("Order", id);
        repo.deleteById(id);
    }

    @Override
    public OrderResponse confirm(UUID id) {
        Order order = find(id);
        for (OrderItem orderItem : orderItemRepo.findByOrderId(id)) {
            if (orderItem.getItem() == null) {
                continue;
            }
            // An item with BOM lines defined for it is "manufactured" (OWN_PRODUCT) —
            // having a recipe is what makes it manufactured, per Item.kind.
            List<BillOfMaterial> bomLines = bomRepo.findByItemId(orderItem.getItem().getId());
            for (BillOfMaterial bomLine : bomLines) {
                long neededQty = bomLine.getQuantity().multiply(orderItem.getQuantity())
                        .setScale(0, RoundingMode.HALF_UP).longValueExact();
                reserveAcrossWarehouses(bomLine.getComponent().getId(), neededQty, order);
            }
        }
        order.setStatus(OrderStatus.CONFIRMED);
        order.setConfirmedAt(LocalDateTime.now());
        return mapper.toResponse(order);
    }

    @Override
    public OrderResponse cancel(UUID id) {
        Order order = find(id);
        for (var reservation : reservationRepo.findByOrderIdAndStatus(id, ReservationStatus.ACTIVE)) {
            reservationService.release(reservation.getId());
        }
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        return mapper.toResponse(order);
    }

    // Reserves the needed quantity of a BOM component, taking from the fullest warehouse first
    // (same heuristic as OrderFulfillmentServiceImpl.issueAcrossWarehouses) and splitting across
    // warehouses if no single one covers it. Throws InsufficientStockException if the total
    // available across all warehouses can't cover it — the whole confirm() then rolls back.
    private void reserveAcrossWarehouses(UUID componentId, long needed, Order order) {
        List<WarehouseStockResponse> perWarehouse = movementRepo.findStockByWarehouseForItem(componentId).stream()
                .filter(w -> w.balance() != null && w.balance() > 0)
                .sorted(Comparator.comparingLong((WarehouseStockResponse w) -> w.balance()).reversed())
                .toList();

        long remaining = needed;
        for (WarehouseStockResponse w : perWarehouse) {
            if (remaining <= 0) {
                break;
            }
            long alreadyReserved = reservationRepo
                    .findByItemIdAndWarehouseIdAndStatus(componentId, w.warehouseId(), ReservationStatus.ACTIVE)
                    .stream().mapToLong(r -> r.getQuantity() != null ? r.getQuantity() : 0L).sum();
            long available = w.balance() - alreadyReserved;
            if (available <= 0) {
                continue;
            }
            long take = Math.min(remaining, available);
            reservationService.create(new CreateReservationRequest(
                    componentId, w.warehouseId(), take,
                    "Reserved for order " + (order.getOrderNumber() != null ? order.getOrderNumber() : order.getId()),
                    order.getId()));
            remaining -= take;
        }

        if (remaining > 0) {
            throw new InsufficientStockException(
                    "Insufficient stock across warehouses for component '%s': missing %d"
                            .formatted(componentId, remaining));
        }
    }

    private Order find(UUID id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Order", id));
    }

    private Customer resolveCustomer(UUID id) {
        if (id == null) return null;
        return customerRepo.findById(id).orElseThrow(() -> new NotFoundException("Customer", id));
    }
}

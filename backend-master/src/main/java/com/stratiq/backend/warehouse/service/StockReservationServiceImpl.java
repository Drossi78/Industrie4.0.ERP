package com.stratiq.backend.warehouse.service;

import com.stratiq.backend.inventory.model.Item;
import com.stratiq.backend.inventory.repository.ItemRepository;
import com.stratiq.backend.order.model.Order;
import com.stratiq.backend.order.repository.OrderRepository;
import com.stratiq.backend.shared.exception.InsufficientStockException;
import com.stratiq.backend.shared.exception.NotFoundException;
import com.stratiq.backend.warehouse.dto.request.CreateReservationRequest;
import com.stratiq.backend.warehouse.dto.response.ReservationResponse;
import com.stratiq.backend.warehouse.model.ReservationStatus;
import com.stratiq.backend.warehouse.model.StockReservation;
import com.stratiq.backend.warehouse.model.Warehouse;
import com.stratiq.backend.warehouse.repository.StockReservationRepository;
import com.stratiq.backend.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class StockReservationServiceImpl implements StockReservationService {

    private final StockReservationRepository repo;
    private final ItemRepository itemRepo;
    private final WarehouseRepository warehouseRepo;
    private final OrderRepository orderRepo;
    private final StockService stockService;

    @Override
    public ReservationResponse create(CreateReservationRequest req) {
        Item item = itemRepo.findById(req.itemId())
                .orElseThrow(() -> new NotFoundException("Item", req.itemId()));
        Warehouse warehouse = warehouseRepo.findById(req.warehouseId())
                .orElseThrow(() -> new NotFoundException("Warehouse", req.warehouseId()));
        Order order = resolveOrder(req.orderId());

        // Available = physical balance minus what is already reserved (ACTIVE) in this warehouse.
        long onHand = stockService.differenceReport(req.itemId(), req.warehouseId()).balance();
        long reserved = activeReservedQuantity(req.itemId(), req.warehouseId());
        long available = onHand - reserved;
        if (available < req.quantity()) {
            throw new InsufficientStockException(
                    "Insufficient available stock: available %d, requested %d".formatted(available, req.quantity()));
        }

        StockReservation reservation = new StockReservation();
        reservation.setItem(item);
        reservation.setWarehouse(warehouse);
        reservation.setOrder(order);
        reservation.setQuantity(req.quantity());
        reservation.setNote(req.note());
        reservation.setStatus(ReservationStatus.ACTIVE);
        return toResponse(repo.save(reservation));
    }

    @Override
    public ReservationResponse release(UUID id) {
        StockReservation reservation = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("StockReservation", id));
        if (reservation.getStatus() == ReservationStatus.ACTIVE) {
            reservation.setStatus(ReservationStatus.RELEASED);
            reservation.setReleasedAt(LocalDateTime.now());
        }
        return toResponse(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> list(UUID warehouseId, UUID itemId, String status) {
        ReservationStatus st = parseStatus(status);
        List<StockReservation> results;
        if (warehouseId != null) {
            results = repo.findByWarehouseIdAndStatusOrderByCreatedAtDesc(warehouseId, st);
        } else if (itemId != null) {
            results = repo.findByItemIdAndStatusOrderByCreatedAtDesc(itemId, st);
        } else {
            results = repo.findByStatusOrderByCreatedAtDesc(st);
        }
        return results.stream().map(this::toResponse).toList();
    }

    // Sum of quantities of ACTIVE reservations for an item in a warehouse.
    private long activeReservedQuantity(UUID itemId, UUID warehouseId) {
        return repo.findByItemIdAndWarehouseIdAndStatus(itemId, warehouseId, ReservationStatus.ACTIVE)
                .stream()
                .mapToLong(r -> r.getQuantity() != null ? r.getQuantity() : 0L)
                .sum();
    }

    private ReservationStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return ReservationStatus.ACTIVE;
        }
        try {
            return ReservationStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid reservation status: " + status);
        }
    }

    private Order resolveOrder(UUID id) {
        if (id == null) return null;
        return orderRepo.findById(id).orElseThrow(() -> new NotFoundException("Order", id));
    }

    private ReservationResponse toResponse(StockReservation r) {
        return new ReservationResponse(
                r.getId(),
                r.getCreatedAt(),
                r.getItem() != null ? r.getItem().getId() : null,
                r.getItem() != null ? r.getItem().getName() : null,
                r.getWarehouse() != null ? r.getWarehouse().getId() : null,
                r.getWarehouse() != null ? r.getWarehouse().getName() : null,
                r.getQuantity(),
                r.getStatus() != null ? r.getStatus().name() : null,
                r.getNote(),
                r.getOrder() != null ? r.getOrder().getId() : null,
                r.getReleasedAt()
        );
    }
}

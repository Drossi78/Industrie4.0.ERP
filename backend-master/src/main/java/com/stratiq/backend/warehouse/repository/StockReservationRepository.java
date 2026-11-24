package com.stratiq.backend.warehouse.repository;

import com.stratiq.backend.warehouse.model.ReservationStatus;
import com.stratiq.backend.warehouse.model.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StockReservationRepository extends JpaRepository<StockReservation, UUID> {

    List<StockReservation> findByStatusOrderByCreatedAtDesc(ReservationStatus status);

    List<StockReservation> findByWarehouseIdAndStatusOrderByCreatedAtDesc(UUID warehouseId, ReservationStatus status);

    List<StockReservation> findByItemIdAndStatusOrderByCreatedAtDesc(UUID itemId, ReservationStatus status);

    // Active reservations for a specific item in a specific warehouse (used to compute available stock).
    List<StockReservation> findByItemIdAndWarehouseIdAndStatus(UUID itemId, UUID warehouseId, ReservationStatus status);

    // All reservations for an order in a given status (e.g. release ACTIVE ones on order cancellation).
    List<StockReservation> findByOrderIdAndStatus(UUID orderId, ReservationStatus status);

    // A specific component's reservations for one order in a given status (e.g. convert to
    // stock-out on production completion — reservations aren't tagged to a specific order line,
    // so this is scoped by component item instead; see OrderProductionStepServiceImpl for why
    // that's sufficient).
    List<StockReservation> findByItemIdAndOrderIdAndStatus(UUID itemId, UUID orderId, ReservationStatus status);
}

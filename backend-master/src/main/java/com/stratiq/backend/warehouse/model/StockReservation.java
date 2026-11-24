package com.stratiq.backend.warehouse.model;

import com.stratiq.backend.inventory.model.Item;
import com.stratiq.backend.order.model.Order;
import com.stratiq.backend.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// A reservation blocks a quantity of an item in a warehouse without changing
// the physical stock. Available stock = physical balance - sum of ACTIVE reservations.
@Entity
@Table(name = "stock_reservation")
@Getter
@Setter
@NoArgsConstructor
public class StockReservation extends BaseEntity {

    private Long quantity;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private ReservationStatus status = ReservationStatus.ACTIVE;

    @Column(length = 255)
    private String note;

    // Set when the reservation is released; null while ACTIVE.
    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    // Optional link to the order this reservation is for.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
}

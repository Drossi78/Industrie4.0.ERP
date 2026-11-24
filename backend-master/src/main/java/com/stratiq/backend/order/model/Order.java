package com.stratiq.backend.order.model;

import com.stratiq.backend.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import com.stratiq.backend.order.model.OrderStatus;

@Entity
@Table(name = "auftrag")
@Getter
@Setter
@NoArgsConstructor
public class Order extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kunden_id")
    private Customer customer;

    @Column(name = "order_number", length = 50)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "order_status_enum")
    private OrderStatus status = OrderStatus.DRAFT;

    // Stock direction chosen at creation: OUT = sale (goods issue), IN = procurement (goods receipt).
    // Stored as plain varchar (enum name), so no Postgres enum type is required.
    @Enumerated(EnumType.STRING)
    @Column(name = "direction", length = 10, nullable = false)
    private OrderDirection direction = OrderDirection.OUT;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "expected_date")
    private LocalDateTime expectedDate;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "total_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(columnDefinition = "text")
    private String notes;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Soft-delete: deletedAt null = active, non-null = deleted (not removed from DB).
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}

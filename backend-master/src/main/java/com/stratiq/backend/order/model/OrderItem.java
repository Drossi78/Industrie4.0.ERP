package com.stratiq.backend.order.model;

import com.stratiq.backend.shared.model.Unit;
import com.stratiq.backend.inventory.model.Supplier;
import com.stratiq.backend.inventory.model.Item;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import com.stratiq.backend.order.model.Order;

@Entity
@Table(name = "auftrag_items")
@Getter
@Setter
@NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auftrag_id", nullable = false)
    private Order order;

    // Cross-domain reference to Item in inventory domain.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    // Item name snapshot at order time (denormalized for historical accuracy).
    @Column(name = "item_name", nullable = false, length = 255)
    private String itemName;

    // Unit enumeration (piece, kg, l, m, etc.). Must match shared Unit enum.
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "unit", nullable = false, columnDefinition = "unit_enum")
    private Unit unit;

    // Quantity in item's unit. Scale: 3 decimals for fractional units.
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    // Price per unit. Scale: 2 decimals (currency, typically EUR).
    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    // Line total = quantity × unitPrice (usually calculated on API).
    @Column(name = "line_total", nullable = false, precision = 14, scale = 2)
    private BigDecimal lineTotal;

    @Column(columnDefinition = "text")
    private String notes;

    // Optional supplier reference. Required if item is PURCHASED.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (lineTotal == null && quantity != null && unitPrice != null) {
            lineTotal = quantity.multiply(unitPrice);
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (quantity != null && unitPrice != null) {
            lineTotal = quantity.multiply(unitPrice);
        }
    }
}

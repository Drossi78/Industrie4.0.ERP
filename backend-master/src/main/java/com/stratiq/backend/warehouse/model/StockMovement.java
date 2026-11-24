package com.stratiq.backend.warehouse.model;

import com.stratiq.backend.order.model.Order;
import com.stratiq.backend.inventory.model.Batch;
import com.stratiq.backend.inventory.model.Item;
import com.stratiq.backend.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.stratiq.backend.warehouse.model.Warehouse;

@Entity
@Table(name = "stock_movement")
@Getter
@Setter
@NoArgsConstructor
public class StockMovement extends BaseEntity {

    @Column(length = 255)
    private String note;

    private Long quantity;

    // Type: IN/OUT (English) or EINGANG/AUSGANG (legacy German). Service handles both.
    @Column(length = 255)
    private String type;

    // Cross-domain: references Item, Order, and Warehouse from their respective domains.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auftrag_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lager_id")
    private Warehouse warehouse;

    // Optional: the inventory lot this movement affects. Null for untracked (pre-batch) stock.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private Batch batch;
}

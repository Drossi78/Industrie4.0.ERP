package com.stratiq.backend.warehouse.repository;

import com.stratiq.backend.warehouse.dto.internal.BatchBalance;
import com.stratiq.backend.warehouse.dto.internal.BatchIdBalance;
import com.stratiq.backend.warehouse.dto.response.StockLevelResponse;
import com.stratiq.backend.warehouse.dto.response.WarehouseStockResponse;
import com.stratiq.backend.warehouse.model.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {
    // Get all movements for an item (IN and OUT flows).
    List<StockMovement> findByItemId(UUID itemId);
    // Get all movements in a specific warehouse.
    List<StockMovement> findByWarehouseId(UUID warehouseId);
    // Get all movements related to a specific order.
    List<StockMovement> findByOrderId(UUID orderId);
    // Get movements by type (IN/OUT or legacy EINGANG/AUSGANG).
    List<StockMovement> findByType(String type);

    // Stock overview: current balance for every item, summed across all warehouses.
    // LEFT JOIN from Item so items without any movement still appear with balance 0.
    @Query("""
        SELECT new com.stratiq.backend.warehouse.dto.response.StockLevelResponse(
            i.id,
            i.name,
            COALESCE(SUM(CASE WHEN upper(m.type) IN ('IN', 'EINGANG') THEN m.quantity ELSE 0L END), 0L),
            COALESCE(SUM(CASE WHEN upper(m.type) IN ('OUT', 'AUSGANG') THEN m.quantity ELSE 0L END), 0L),
            COALESCE(SUM(CASE WHEN upper(m.type) IN ('IN', 'EINGANG') THEN m.quantity
                              WHEN upper(m.type) IN ('OUT', 'AUSGANG') THEN -m.quantity
                              ELSE 0L END), 0L)
        )
        FROM Item i
        LEFT JOIN StockMovement m ON m.item = i
        GROUP BY i.id, i.name
        ORDER BY i.name
        """)
    List<StockLevelResponse> findStockLevels();

    // Per-warehouse breakdown of one item's stock. Only warehouses with movements appear.
    @Query("""
        SELECT new com.stratiq.backend.warehouse.dto.response.WarehouseStockResponse(
            w.id,
            w.name,
            COALESCE(SUM(CASE WHEN upper(m.type) IN ('IN', 'EINGANG') THEN m.quantity ELSE 0L END), 0L),
            COALESCE(SUM(CASE WHEN upper(m.type) IN ('OUT', 'AUSGANG') THEN m.quantity ELSE 0L END), 0L),
            COALESCE(SUM(CASE WHEN upper(m.type) IN ('IN', 'EINGANG') THEN m.quantity
                              WHEN upper(m.type) IN ('OUT', 'AUSGANG') THEN -m.quantity
                              ELSE 0L END), 0L)
        )
        FROM StockMovement m
        JOIN m.warehouse w
        WHERE m.item.id = :itemId
        GROUP BY w.id, w.name
        ORDER BY w.name
        """)
    List<WarehouseStockResponse> findStockByWarehouseForItem(@Param("itemId") UUID itemId);

    // Per-item stock levels within a single warehouse.
    // Only items that have movements in this warehouse appear.
    @Query("""
        SELECT new com.stratiq.backend.warehouse.dto.response.StockLevelResponse(
            i.id,
            i.name,
            COALESCE(SUM(CASE WHEN upper(m.type) IN ('IN', 'EINGANG') THEN m.quantity ELSE 0L END), 0L),
            COALESCE(SUM(CASE WHEN upper(m.type) IN ('OUT', 'AUSGANG') THEN m.quantity ELSE 0L END), 0L),
            COALESCE(SUM(CASE WHEN upper(m.type) IN ('IN', 'EINGANG') THEN m.quantity
                              WHEN upper(m.type) IN ('OUT', 'AUSGANG') THEN -m.quantity
                              ELSE 0L END), 0L)
        )
        FROM StockMovement m
        JOIN m.item i
        WHERE m.warehouse.id = :warehouseId
        GROUP BY i.id, i.name
        ORDER BY i.name
        """)
    List<StockLevelResponse> findStockLevelsByWarehouse(@Param("warehouseId") UUID warehouseId);

    // Per-batch available balance for one item in one warehouse, restricted to batches with
    // remaining stock (> 0). Ordered FEFO first (earliest expiry, nulls last), then FIFO
    // (oldest received). This drives automatic allocation during a goods issue.
    @Query("""
        SELECT new com.stratiq.backend.warehouse.dto.internal.BatchBalance(
            b,
            COALESCE(SUM(CASE WHEN upper(m.type) IN ('IN', 'EINGANG') THEN m.quantity
                              WHEN upper(m.type) IN ('OUT', 'AUSGANG') THEN -m.quantity
                              ELSE 0L END), 0L)
        )
        FROM StockMovement m
        JOIN m.batch b
        WHERE m.item.id = :itemId AND m.warehouse.id = :warehouseId
        GROUP BY b
        HAVING COALESCE(SUM(CASE WHEN upper(m.type) IN ('IN', 'EINGANG') THEN m.quantity
                                 WHEN upper(m.type) IN ('OUT', 'AUSGANG') THEN -m.quantity
                                 ELSE 0L END), 0L) > 0
        ORDER BY b.expiryDate ASC NULLS LAST, b.createdAt ASC
        """)
    List<BatchBalance> findBatchBalances(@Param("itemId") UUID itemId,
                                         @Param("warehouseId") UUID warehouseId);

    // Available balance of a single batch (SUM(IN) - SUM(OUT)). Used for manual batch selection.
    @Query("""
        SELECT COALESCE(SUM(CASE WHEN upper(m.type) IN ('IN', 'EINGANG') THEN m.quantity
                                 WHEN upper(m.type) IN ('OUT', 'AUSGANG') THEN -m.quantity
                                 ELSE 0L END), 0L)
        FROM StockMovement m
        WHERE m.batch.id = :batchId
        """)
    long findBatchBalance(@Param("batchId") UUID batchId);

    // Derived balance per batch for a set of batches. Batches without any movement do not
    // appear in the result (their balance is 0). Used to enrich batch reads with current stock.
    @Query("""
        SELECT new com.stratiq.backend.warehouse.dto.internal.BatchIdBalance(
            m.batch.id,
            COALESCE(SUM(CASE WHEN upper(m.type) IN ('IN', 'EINGANG') THEN m.quantity
                              WHEN upper(m.type) IN ('OUT', 'AUSGANG') THEN -m.quantity
                              ELSE 0L END), 0L)
        )
        FROM StockMovement m
        WHERE m.batch.id IN :batchIds
        GROUP BY m.batch.id
        """)
    List<BatchIdBalance> findBalancesForBatches(@Param("batchIds") Collection<UUID> batchIds);
}

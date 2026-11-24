package com.stratiq.backend.warehouse.service;

import com.stratiq.backend.warehouse.dto.request.AdjustmentRequest;
import com.stratiq.backend.warehouse.dto.request.GoodsIssueRequest;
import com.stratiq.backend.warehouse.dto.request.GoodsReceiptRequest;
import com.stratiq.backend.warehouse.dto.request.StockTransferRequest;
import com.stratiq.backend.warehouse.dto.response.BatchStockResponse;
import com.stratiq.backend.warehouse.dto.response.DifferenceReportResponse;
import com.stratiq.backend.warehouse.dto.response.StockLevelResponse;
import com.stratiq.backend.warehouse.dto.response.StockMovementResponse;
import com.stratiq.backend.warehouse.dto.response.WarehouseStockResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface StockService {
    Page<StockMovementResponse> findAll(Pageable pageable);
    StockMovementResponse adjustment(AdjustmentRequest req);
    DifferenceReportResponse differenceReport(UUID itemId, UUID warehouseId);

    // Wareneingang: book a positive IN movement into a warehouse.
    StockMovementResponse goodsReceipt(GoodsReceiptRequest req);

    // Warenausgang: book OUT movement(s); fails if stock is insufficient. Automatic batch
    // allocation (FEFO-then-FIFO) may split the issue across several lots, hence a list.
    List<StockMovementResponse> goodsIssue(GoodsIssueRequest req);

    // Umlagerung: move stock between two warehouses (OUT + IN in one transaction).
    List<StockMovementResponse> transfer(StockTransferRequest req);

    // Current stock level for every item (summed across warehouses).
    List<StockLevelResponse> getStockLevels();

    // Per-warehouse breakdown of one item's stock.
    List<WarehouseStockResponse> getStockByWarehouse(UUID itemId);

    // Per-item stock levels within a single warehouse.
    List<StockLevelResponse> getStockLevelsForWarehouse(UUID warehouseId);

    // Available batches (balance > 0) for an item in a warehouse, ordered FEFO-then-FIFO.
    // Drives the batch picker for a manual goods issue.
    List<BatchStockResponse> getBatchStock(UUID itemId, UUID warehouseId);
}

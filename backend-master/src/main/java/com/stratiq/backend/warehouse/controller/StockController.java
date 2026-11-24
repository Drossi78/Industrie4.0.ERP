package com.stratiq.backend.warehouse.controller;

import com.stratiq.backend.warehouse.dto.request.AdjustmentRequest;
import com.stratiq.backend.warehouse.dto.request.GoodsIssueRequest;
import com.stratiq.backend.warehouse.dto.request.GoodsReceiptRequest;
import com.stratiq.backend.warehouse.dto.request.StockTransferRequest;
import com.stratiq.backend.warehouse.dto.response.BatchStockResponse;
import com.stratiq.backend.warehouse.dto.response.DifferenceReportResponse;
import com.stratiq.backend.warehouse.dto.response.StockLevelResponse;
import com.stratiq.backend.warehouse.dto.response.StockMovementResponse;
import com.stratiq.backend.warehouse.dto.response.WarehouseStockResponse;
import com.stratiq.backend.warehouse.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService service;

    @GetMapping
    @PreAuthorize("hasAuthority('warehouse:read')")
    public ResponseEntity<DifferenceReportResponse> stockInfo(
            @RequestParam(required = false) UUID itemId,
            @RequestParam(required = false) UUID warehouseId) {
        return ResponseEntity.ok(service.differenceReport(itemId, warehouseId));
    }

    @GetMapping("/difference-report")
    @PreAuthorize("hasAuthority('warehouse:read')")
    public ResponseEntity<DifferenceReportResponse> report(
            @RequestParam(required = false) UUID itemId,
            @RequestParam(required = false) UUID warehouseId) {
        return ResponseEntity.ok(service.differenceReport(itemId, warehouseId));
    }

    @GetMapping("/movements")
    @PreAuthorize("hasAuthority('warehouse:read')")
    public ResponseEntity<Page<StockMovementResponse>> movements(Pageable pageable) {
        return ResponseEntity.ok(service.findAll(pageable));
    }

    @GetMapping("/levels")
    @PreAuthorize("hasAuthority('warehouse:read')")
    @Operation(summary = "Stock overview: current balance for every item, summed across warehouses")
    public ResponseEntity<List<StockLevelResponse>> levels() {
        return ResponseEntity.ok(service.getStockLevels());
    }

    @GetMapping("/levels/{itemId}/warehouses")
    @PreAuthorize("hasAuthority('warehouse:read')")
    @Operation(summary = "Per-warehouse breakdown of one item's stock")
    public ResponseEntity<List<WarehouseStockResponse>> levelsByWarehouse(@PathVariable UUID itemId) {
        return ResponseEntity.ok(service.getStockByWarehouse(itemId));
    }

    @GetMapping("/warehouses/{warehouseId}/levels")
    @PreAuthorize("hasAuthority('warehouse:read')")
    @Operation(summary = "Per-item stock levels within a single warehouse")
    public ResponseEntity<List<StockLevelResponse>> levelsForWarehouse(@PathVariable UUID warehouseId) {
        return ResponseEntity.ok(service.getStockLevelsForWarehouse(warehouseId));
    }

    @GetMapping("/batches")
    @PreAuthorize("hasAuthority('warehouse:read')")
    @Operation(summary = "Available batches (balance > 0) for an item in a warehouse, ordered FEFO/FIFO")
    public ResponseEntity<List<BatchStockResponse>> batchStock(
            @RequestParam UUID itemId,
            @RequestParam UUID warehouseId) {
        return ResponseEntity.ok(service.getBatchStock(itemId, warehouseId));
    }

    @PostMapping("/adjustment")
    @PreAuthorize("hasAuthority('warehouse:write')")
    public ResponseEntity<StockMovementResponse> adjustment(@Valid @RequestBody AdjustmentRequest req) {
        return ResponseEntity.status(201).body(service.adjustment(req));
    }

    @PostMapping("/goods-receipt")
    @PreAuthorize("hasAuthority('warehouse:write')")
    @Operation(summary = "Wareneingang: book an IN movement into a warehouse")
    public ResponseEntity<StockMovementResponse> goodsReceipt(@Valid @RequestBody GoodsReceiptRequest req) {
        return ResponseEntity.status(201).body(service.goodsReceipt(req));
    }

    @PostMapping("/goods-issue")
    @PreAuthorize("hasAuthority('warehouse:write')")
    @Operation(summary = "Warenausgang: book OUT movement(s); auto-allocates across batches (FEFO/FIFO)")
    public ResponseEntity<List<StockMovementResponse>> goodsIssue(@Valid @RequestBody GoodsIssueRequest req) {
        return ResponseEntity.status(201).body(service.goodsIssue(req));
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasAuthority('warehouse:write')")
    @Operation(summary = "Umlagerung: move stock between two warehouses")
    public ResponseEntity<List<StockMovementResponse>> transfer(@Valid @RequestBody StockTransferRequest req) {
        return ResponseEntity.status(201).body(service.transfer(req));
    }
}

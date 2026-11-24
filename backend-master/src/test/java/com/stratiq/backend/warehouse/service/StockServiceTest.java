package com.stratiq.backend.warehouse.service;

import com.stratiq.backend.inventory.model.Batch;
import com.stratiq.backend.inventory.model.Item;
import com.stratiq.backend.inventory.repository.BatchRepository;
import com.stratiq.backend.inventory.repository.ItemRepository;
import com.stratiq.backend.shared.exception.InsufficientStockException;
import com.stratiq.backend.shared.exception.NotFoundException;
import com.stratiq.backend.warehouse.dto.internal.BatchBalance;
import com.stratiq.backend.warehouse.dto.request.AdjustmentRequest;
import com.stratiq.backend.warehouse.dto.request.GoodsIssueRequest;
import com.stratiq.backend.warehouse.dto.request.GoodsReceiptRequest;
import com.stratiq.backend.warehouse.mapper.StockMovementMapper;
import com.stratiq.backend.warehouse.model.StockMovement;
import com.stratiq.backend.warehouse.model.Warehouse;
import com.stratiq.backend.warehouse.repository.StockMovementRepository;
import com.stratiq.backend.warehouse.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private BatchRepository batchRepository;

    @Mock
    private StockMovementMapper mapper;

    @InjectMocks
    private StockServiceImpl stockService;

    private StockMovement testMovement;
    private Item testItem;
    private Warehouse testWarehouse;
    private UUID testItemId;
    private UUID testWarehouseId;

    @BeforeEach
    void setUp() {
        testItemId = UUID.randomUUID();
        testWarehouseId = UUID.randomUUID();

        testItem = new Item();
        testItem.setId(testItemId);
        testItem.setName("Widget A");

        testWarehouse = new Warehouse();
        testWarehouse.setId(testWarehouseId);
        testWarehouse.setName("Main Warehouse");

        testMovement = new StockMovement();
        testMovement.setId(UUID.randomUUID());
        testMovement.setQuantity(100L);
        testMovement.setType("IN");
        testMovement.setItem(testItem);
        testMovement.setWarehouse(testWarehouse);
    }

    @Test
    void testFindAll_Success() {
        var pageable = PageRequest.of(0, 10);
        when(stockMovementRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(testMovement)));

        stockService.findAll(pageable);

        verify(stockMovementRepository, times(1)).findAll(pageable);
    }

    @Test
    void testAdjustment_InMovement_Success() {
        AdjustmentRequest request = new AdjustmentRequest(testItemId, testWarehouseId, 50L, "Stock IN");

        when(itemRepository.findById(testItemId)).thenReturn(Optional.of(testItem));
        when(warehouseRepository.findById(testWarehouseId)).thenReturn(Optional.of(testWarehouse));
        when(stockMovementRepository.save(any(StockMovement.class))).thenReturn(testMovement);

        stockService.adjustment(request);

        verify(stockMovementRepository, times(1)).save(any(StockMovement.class));
    }

    @Test
    void testAdjustment_OutMovement_Success() {
        AdjustmentRequest request = new AdjustmentRequest(testItemId, testWarehouseId, -30L, "Stock OUT");

        when(itemRepository.findById(testItemId)).thenReturn(Optional.of(testItem));
        when(warehouseRepository.findById(testWarehouseId)).thenReturn(Optional.of(testWarehouse));
        when(stockMovementRepository.save(any(StockMovement.class))).thenReturn(testMovement);

        stockService.adjustment(request);

        verify(stockMovementRepository, times(1)).save(any(StockMovement.class));
    }

    @Test
    void testAdjustment_ItemNotFound() {
        AdjustmentRequest request = new AdjustmentRequest(testItemId, testWarehouseId, 50L, "note");

        when(itemRepository.findById(testItemId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> stockService.adjustment(request));
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    void testDifferenceReport_Success() {
        StockMovement inMovement = new StockMovement();
        inMovement.setType("IN");
        inMovement.setQuantity(100L);
        inMovement.setItem(testItem);
        inMovement.setWarehouse(testWarehouse);

        StockMovement outMovement = new StockMovement();
        outMovement.setType("OUT");
        outMovement.setQuantity(30L);
        outMovement.setItem(testItem);
        outMovement.setWarehouse(testWarehouse);

        when(stockMovementRepository.findByItemId(testItemId)).thenReturn(List.of(inMovement, outMovement));
        when(itemRepository.findById(testItemId)).thenReturn(Optional.of(testItem));
        when(warehouseRepository.findById(testWarehouseId)).thenReturn(Optional.of(testWarehouse));

        var result = stockService.differenceReport(testItemId, testWarehouseId);

        verify(stockMovementRepository, times(1)).findByItemId(testItemId);
    }

    @Test
    void testDifferenceReport_NoWarehouseFilter() {
        when(stockMovementRepository.findByItemId(testItemId)).thenReturn(List.of(testMovement));
        when(itemRepository.findById(testItemId)).thenReturn(Optional.of(testItem));

        stockService.differenceReport(testItemId, null);

        verify(stockMovementRepository, times(1)).findByItemId(testItemId);
    }

    @Test
    void testGoodsReceipt_withNewBatch_createsBatchAndBooksIn() {
        GoodsReceiptRequest request = new GoodsReceiptRequest(
                testItemId, testWarehouseId, 50L, "CH-001", null, "Wareneingang", null);

        Batch batch = new Batch();
        batch.setId(UUID.randomUUID());
        batch.setBatchNumber("CH-001");

        when(itemRepository.findById(testItemId)).thenReturn(Optional.of(testItem));
        when(warehouseRepository.findById(testWarehouseId)).thenReturn(Optional.of(testWarehouse));
        when(batchRepository.findByBatchNumberAndItemIdAndWarehouseId("CH-001", testItemId, testWarehouseId))
                .thenReturn(Optional.empty());
        when(batchRepository.save(any(Batch.class))).thenReturn(batch);
        when(stockMovementRepository.save(any(StockMovement.class))).thenReturn(testMovement);

        stockService.goodsReceipt(request);

        // A new lot is created and exactly one IN movement is booked onto it.
        verify(batchRepository, times(1)).save(any(Batch.class));
        verify(stockMovementRepository, times(1)).save(any(StockMovement.class));
    }

    @Test
    void testGoodsIssue_autoAllocation_spansMultipleBatches() {
        // Request 40; batch balances are 20 (CH-1) and 30 (CH-2) -> take 20 + 20 across two lots.
        GoodsIssueRequest request = new GoodsIssueRequest(
                testItemId, testWarehouseId, 40L, null, "Ausgang", null);

        Batch b1 = new Batch();
        b1.setId(UUID.randomUUID());
        Batch b2 = new Batch();
        b2.setId(UUID.randomUUID());

        when(itemRepository.findById(testItemId)).thenReturn(Optional.of(testItem));
        when(warehouseRepository.findById(testWarehouseId)).thenReturn(Optional.of(testWarehouse));
        when(stockMovementRepository.findBatchBalances(testItemId, testWarehouseId))
                .thenReturn(List.of(new BatchBalance(b1, 20L), new BatchBalance(b2, 30L)));
        when(stockMovementRepository.save(any(StockMovement.class))).thenReturn(testMovement);

        var result = stockService.goodsIssue(request);

        // Two OUT movements: one per consumed lot.
        assertEquals(2, result.size());
        verify(stockMovementRepository, times(2)).save(any(StockMovement.class));
    }

    @Test
    void testGoodsIssue_autoAllocation_insufficientTotal_throws() {
        GoodsIssueRequest request = new GoodsIssueRequest(
                testItemId, testWarehouseId, 100L, null, "Ausgang", null);

        Batch b1 = new Batch();
        b1.setId(UUID.randomUUID());

        when(itemRepository.findById(testItemId)).thenReturn(Optional.of(testItem));
        when(warehouseRepository.findById(testWarehouseId)).thenReturn(Optional.of(testWarehouse));
        when(stockMovementRepository.findBatchBalances(testItemId, testWarehouseId))
                .thenReturn(List.of(new BatchBalance(b1, 30L)));

        assertThrows(InsufficientStockException.class, () -> stockService.goodsIssue(request));
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    void testGoodsIssue_manualBatch_insufficient_throws() {
        UUID batchId = UUID.randomUUID();
        GoodsIssueRequest request = new GoodsIssueRequest(
                testItemId, testWarehouseId, 40L, batchId, "Ausgang", null);

        Batch batch = new Batch();
        batch.setId(batchId);
        batch.setBatchNumber("CH-001");

        when(itemRepository.findById(testItemId)).thenReturn(Optional.of(testItem));
        when(warehouseRepository.findById(testWarehouseId)).thenReturn(Optional.of(testWarehouse));
        when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));
        when(stockMovementRepository.findBatchBalance(batchId)).thenReturn(5L);

        assertThrows(InsufficientStockException.class, () -> stockService.goodsIssue(request));
        verify(stockMovementRepository, never()).save(any());
    }
}

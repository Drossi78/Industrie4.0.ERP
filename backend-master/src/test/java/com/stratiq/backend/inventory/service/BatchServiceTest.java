package com.stratiq.backend.inventory.service;

import com.stratiq.backend.inventory.dto.request.BatchCreateRequest;
import com.stratiq.backend.inventory.dto.request.BatchUpdateRequest;
import com.stratiq.backend.inventory.mapper.BatchMapper;
import com.stratiq.backend.inventory.model.Batch;
import com.stratiq.backend.inventory.model.Item;
import com.stratiq.backend.inventory.repository.BatchRepository;
import com.stratiq.backend.inventory.repository.ItemRepository;
import com.stratiq.backend.shared.exception.NotFoundException;
import com.stratiq.backend.warehouse.model.Warehouse;
import com.stratiq.backend.warehouse.repository.StockMovementRepository;
import com.stratiq.backend.warehouse.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchServiceTest {

    @Mock
    private BatchRepository batchRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private StockMovementRepository movementRepository;

    @Mock
    private BatchMapper mapper;

    @InjectMocks
    private BatchServiceImpl batchService;

    private Batch testBatch;
    private Item testItem;
    private Warehouse testWarehouse;
    private UUID testBatchId;
    private UUID testItemId;
    private UUID testWarehouseId;

    @BeforeEach
    void setUp() {
        testBatchId = UUID.randomUUID();
        testItemId = UUID.randomUUID();
        testWarehouseId = UUID.randomUUID();

        testItem = new Item();
        testItem.setId(testItemId);
        testItem.setName("Widget");

        testWarehouse = new Warehouse();
        testWarehouse.setId(testWarehouseId);
        testWarehouse.setName("Main Warehouse");

        testBatch = new Batch();
        testBatch.setId(testBatchId);
        testBatch.setBatchNumber("BATCH-001");
        testBatch.setItem(testItem);
        testBatch.setWarehouse(testWarehouse);
    }

    @Test
    void testFindAll_Success() {
        when(batchRepository.findAll()).thenReturn(List.of(testBatch));

        batchService.findAll();

        verify(batchRepository, times(1)).findAll();
    }

    @Test
    void testFindById_Success() {
        when(batchRepository.findById(testBatchId)).thenReturn(Optional.of(testBatch));

        batchService.findById(testBatchId);

        verify(batchRepository, times(1)).findById(testBatchId);
    }

    @Test
    void testFindById_NotFound() {
        when(batchRepository.findById(testBatchId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> batchService.findById(testBatchId));
    }

    @Test
    void testFindByItem_Success() {
        when(batchRepository.findByItemId(testItemId)).thenReturn(List.of(testBatch));

        batchService.findByItem(testItemId);

        verify(batchRepository, times(1)).findByItemId(testItemId);
    }

    @Test
    void testFindByWarehouse_Success() {
        when(batchRepository.findByWarehouseId(testWarehouseId)).thenReturn(List.of(testBatch));

        batchService.findByWarehouse(testWarehouseId);

        verify(batchRepository, times(1)).findByWarehouseId(testWarehouseId);
    }

    @Test
    void testCurrentStock_Success() {
        when(batchRepository.findById(testBatchId)).thenReturn(Optional.of(testBatch));

        batchService.currentStock(testBatchId);

        verify(batchRepository, times(1)).findById(testBatchId);
    }

    @Test
    void testCreateBatch_Success() {
        BatchCreateRequest request = new BatchCreateRequest(
            "BATCH-001", testItemId, testWarehouseId, null
        );

        when(itemRepository.findById(testItemId)).thenReturn(Optional.of(testItem));
        when(warehouseRepository.findById(testWarehouseId)).thenReturn(Optional.of(testWarehouse));
        when(batchRepository.save(any(Batch.class))).thenReturn(testBatch);

        batchService.create(request);

        verify(itemRepository, times(1)).findById(testItemId);
        verify(warehouseRepository, times(1)).findById(testWarehouseId);
        verify(batchRepository, times(1)).save(any(Batch.class));
    }

    @Test
    void testCreateBatch_ItemNotFound() {
        BatchCreateRequest request = new BatchCreateRequest(
            "BATCH-001", testItemId, testWarehouseId, null
        );

        when(itemRepository.findById(testItemId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> batchService.create(request));
        verify(batchRepository, never()).save(any());
    }

    @Test
    void testCreateBatch_WarehouseNotFound() {
        BatchCreateRequest request = new BatchCreateRequest(
            "BATCH-001", testItemId, testWarehouseId, null
        );

        when(itemRepository.findById(testItemId)).thenReturn(Optional.of(testItem));
        when(warehouseRepository.findById(testWarehouseId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> batchService.create(request));
        verify(batchRepository, never()).save(any());
    }

    @Test
    void testUpdateBatch_Success() {
        BatchUpdateRequest request = new BatchUpdateRequest("BATCH-002", null, null, null);

        when(batchRepository.findById(testBatchId)).thenReturn(Optional.of(testBatch));
        when(batchRepository.save(testBatch)).thenReturn(testBatch);

        batchService.update(testBatchId, request);

        verify(batchRepository, times(1)).findById(testBatchId);
        verify(batchRepository, times(1)).save(testBatch);
    }

    @Test
    void testDeleteBatch_Success() {
        when(batchRepository.existsById(testBatchId)).thenReturn(true);

        batchService.delete(testBatchId);

        verify(batchRepository, times(1)).deleteById(testBatchId);
    }

    @Test
    void testDeleteBatch_NotFound() {
        when(batchRepository.existsById(testBatchId)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> batchService.delete(testBatchId));
        verify(batchRepository, never()).deleteById(any());
    }
}

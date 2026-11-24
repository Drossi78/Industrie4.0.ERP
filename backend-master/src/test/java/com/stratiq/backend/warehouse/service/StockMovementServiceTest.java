package com.stratiq.backend.warehouse.service;

import com.stratiq.backend.inventory.model.Item;
import com.stratiq.backend.inventory.repository.ItemRepository;
import com.stratiq.backend.order.model.Order;
import com.stratiq.backend.order.repository.OrderRepository;
import com.stratiq.backend.shared.exception.NotFoundException;
import com.stratiq.backend.warehouse.dto.request.StockMovementCreateRequest;
import com.stratiq.backend.warehouse.dto.request.StockMovementUpdateRequest;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockMovementServiceTest {

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private StockMovementMapper mapper;

    @InjectMocks
    private StockMovementServiceImpl stockMovementService;

    private StockMovement testMovement;
    private Item testItem;
    private Order testOrder;
    private Warehouse testWarehouse;
    private UUID testMovementId;
    private UUID testItemId;
    private UUID testOrderId;
    private UUID testWarehouseId;

    @BeforeEach
    void setUp() {
        testMovementId = UUID.randomUUID();
        testItemId = UUID.randomUUID();
        testOrderId = UUID.randomUUID();
        testWarehouseId = UUID.randomUUID();

        testItem = new Item();
        testItem.setId(testItemId);

        testOrder = new Order();
        testOrder.setId(testOrderId);

        testWarehouse = new Warehouse();
        testWarehouse.setId(testWarehouseId);

        testMovement = new StockMovement();
        testMovement.setId(testMovementId);
        testMovement.setQuantity(100L);
        testMovement.setType("IN");
        testMovement.setItem(testItem);
        testMovement.setWarehouse(testWarehouse);
    }

    @Test
    void testCreate_Success() {
        StockMovementCreateRequest request = new StockMovementCreateRequest(
            "Incoming stock", 100L, "IN", testItemId, null, testWarehouseId
        );

        when(itemRepository.findById(testItemId)).thenReturn(Optional.of(testItem));
        when(warehouseRepository.findById(testWarehouseId)).thenReturn(Optional.of(testWarehouse));
        when(mapper.toEntity(any(), any(), any(), any())).thenReturn(testMovement);
        when(stockMovementRepository.save(testMovement)).thenReturn(testMovement);

        stockMovementService.create(request);

        verify(itemRepository, times(1)).findById(testItemId);
        verify(warehouseRepository, times(1)).findById(testWarehouseId);
        verify(stockMovementRepository, times(1)).save(testMovement);
    }

    @Test
    void testCreate_ItemNotFound() {
        StockMovementCreateRequest request = new StockMovementCreateRequest(
            "note", 100L, "IN", testItemId, null, testWarehouseId
        );

        when(itemRepository.findById(testItemId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> stockMovementService.create(request));
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    void testCreate_WarehouseNotFound() {
        StockMovementCreateRequest request = new StockMovementCreateRequest(
            "note", 100L, "IN", testItemId, null, testWarehouseId
        );

        when(itemRepository.findById(testItemId)).thenReturn(Optional.of(testItem));
        when(warehouseRepository.findById(testWarehouseId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> stockMovementService.create(request));
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    void testGet_Success() {
        when(stockMovementRepository.findById(testMovementId)).thenReturn(Optional.of(testMovement));

        stockMovementService.get(testMovementId);

        verify(stockMovementRepository, times(1)).findById(testMovementId);
    }

    @Test
    void testGet_NotFound() {
        when(stockMovementRepository.findById(testMovementId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> stockMovementService.get(testMovementId));
    }

    @Test
    void testList_Success() {
        var pageable = PageRequest.of(0, 10);
        when(stockMovementRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(testMovement)));

        stockMovementService.list(pageable);

        verify(stockMovementRepository, times(1)).findAll(pageable);
    }

    @Test
    void testFindByItem_Success() {
        when(stockMovementRepository.findByItemId(testItemId)).thenReturn(List.of(testMovement));

        stockMovementService.findByItem(testItemId);

        verify(stockMovementRepository, times(1)).findByItemId(testItemId);
    }

    @Test
    void testFindByWarehouse_Success() {
        when(stockMovementRepository.findByWarehouseId(testWarehouseId)).thenReturn(List.of(testMovement));

        stockMovementService.findByWarehouse(testWarehouseId);

        verify(stockMovementRepository, times(1)).findByWarehouseId(testWarehouseId);
    }

    @Test
    void testFindByOrder_Success() {
        when(stockMovementRepository.findByOrderId(testOrderId)).thenReturn(List.of(testMovement));

        stockMovementService.findByOrder(testOrderId);

        verify(stockMovementRepository, times(1)).findByOrderId(testOrderId);
    }

    @Test
    void testFindByType_Success() {
        when(stockMovementRepository.findByType("IN")).thenReturn(List.of(testMovement));

        stockMovementService.findByType("IN");

        verify(stockMovementRepository, times(1)).findByType("IN");
    }

    @Test
    void testDelete_Success() {
        when(stockMovementRepository.existsById(testMovementId)).thenReturn(true);

        stockMovementService.delete(testMovementId);

        verify(stockMovementRepository, times(1)).deleteById(testMovementId);
    }

    @Test
    void testDelete_NotFound() {
        when(stockMovementRepository.existsById(testMovementId)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> stockMovementService.delete(testMovementId));
        verify(stockMovementRepository, never()).deleteById(any());
    }
}

package com.stratiq.backend.production.service;

import com.stratiq.backend.inventory.model.Item;
import com.stratiq.backend.inventory.model.BillOfMaterial;
import com.stratiq.backend.inventory.repository.BillOfMaterialRepository;
import com.stratiq.backend.order.model.Order;
import com.stratiq.backend.order.model.OrderItem;
import com.stratiq.backend.order.model.OrderStatus;
import com.stratiq.backend.order.repository.OrderItemRepository;
import com.stratiq.backend.order.repository.OrderRepository;
import com.stratiq.backend.production.dto.request.OrderProductionStepCreateRequest;
import com.stratiq.backend.production.dto.request.OrderProductionStepUpdateRequest;
import com.stratiq.backend.production.dto.request.ProductionStepPatchRequest;
import com.stratiq.backend.production.mapper.OrderProductionStepMapper;
import com.stratiq.backend.production.model.OrderProductionStep;
import com.stratiq.backend.production.model.ProductionStep;
import com.stratiq.backend.production.model.ProductionStepStatus;
import com.stratiq.backend.production.repository.OrderProductionStepRepository;
import com.stratiq.backend.production.repository.ProductionStepRepository;
import com.stratiq.backend.shared.exception.NotFoundException;
import com.stratiq.backend.warehouse.model.ReservationStatus;
import com.stratiq.backend.warehouse.model.StockReservation;
import com.stratiq.backend.warehouse.model.Warehouse;
import com.stratiq.backend.warehouse.repository.StockReservationRepository;
import com.stratiq.backend.warehouse.service.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderProductionStepServiceTest {

    @Mock
    private OrderProductionStepRepository opsRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductionStepRepository productionStepRepository;

    @Mock
    private BillOfMaterialRepository bomRepository;

    @Mock
    private StockReservationRepository reservationRepository;

    @Mock
    private StockService stockService;

    @Mock
    private OrderProductionStepMapper mapper;

    @InjectMocks
    private OrderProductionStepServiceImpl opsService;

    private OrderProductionStep testOps;
    private Order testOrder;
    private OrderItem testOrderItem;
    private Item testProducedItem;
    private ProductionStep testProductionStep;
    private UUID testOpsId;
    private UUID testOrderId;
    private UUID testOrderItemId;
    private UUID testTemplateStepId;

    @BeforeEach
    void setUp() {
        testOpsId = UUID.randomUUID();
        testOrderId = UUID.randomUUID();
        testOrderItemId = UUID.randomUUID();
        testTemplateStepId = UUID.randomUUID();

        testOrder = new Order();
        testOrder.setId(testOrderId);

        testProducedItem = new Item();
        testProducedItem.setId(UUID.randomUUID());

        testOrderItem = new OrderItem();
        testOrderItem.setId(testOrderItemId);
        testOrderItem.setItem(testProducedItem);
        testOrderItem.setQuantity(BigDecimal.ONE);

        testProductionStep = new ProductionStep();
        testProductionStep.setId(testTemplateStepId);

        testOps = new OrderProductionStep();
        testOps.setId(testOpsId);
        testOps.setOrder(testOrder);
        testOps.setOrderItem(testOrderItem);
        testOps.setTemplateStep(testProductionStep);
        testOps.setName("Cutting");
        testOps.setStepOrder(1);
        testOps.setStatus(ProductionStepStatus.PENDING);
    }

    @Test
    void testCreate_Success() {
        OrderProductionStepCreateRequest request = new OrderProductionStepCreateRequest(
            testOrderId, testOrderItemId, testTemplateStepId,
            "Cutting", null, 1, ProductionStepStatus.PENDING, null
        );

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
        when(orderItemRepository.findById(testOrderItemId)).thenReturn(Optional.of(testOrderItem));
        when(productionStepRepository.findById(testTemplateStepId)).thenReturn(Optional.of(testProductionStep));
        when(mapper.toEntity(any(), any(), any(), any())).thenReturn(testOps);
        when(opsRepository.save(testOps)).thenReturn(testOps);

        opsService.create(request);

        verify(orderRepository, times(1)).findById(testOrderId);
        verify(orderItemRepository, times(1)).findById(testOrderItemId);
        verify(productionStepRepository, times(1)).findById(testTemplateStepId);
        verify(opsRepository, times(1)).save(testOps);
    }

    @Test
    void testCreate_OrderNotFound() {
        OrderProductionStepCreateRequest request = new OrderProductionStepCreateRequest(
            testOrderId, testOrderItemId, testTemplateStepId,
            "Cutting", null, 1, null, null
        );

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> opsService.create(request));
        verify(opsRepository, never()).save(any());
    }

    @Test
    void testCreate_OrderItemNotFound() {
        OrderProductionStepCreateRequest request = new OrderProductionStepCreateRequest(
            testOrderId, testOrderItemId, testTemplateStepId,
            "Cutting", null, 1, null, null
        );

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
        when(orderItemRepository.findById(testOrderItemId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> opsService.create(request));
        verify(opsRepository, never()).save(any());
    }

    @Test
    void testCreate_ProductionStepNotFound() {
        OrderProductionStepCreateRequest request = new OrderProductionStepCreateRequest(
            testOrderId, testOrderItemId, testTemplateStepId,
            "Cutting", null, 1, null, null
        );

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
        when(orderItemRepository.findById(testOrderItemId)).thenReturn(Optional.of(testOrderItem));
        when(productionStepRepository.findById(testTemplateStepId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> opsService.create(request));
        verify(opsRepository, never()).save(any());
    }

    @Test
    void testGet_Success() {
        when(opsRepository.findById(testOpsId)).thenReturn(Optional.of(testOps));

        opsService.get(testOpsId);

        verify(opsRepository, times(1)).findById(testOpsId);
    }

    @Test
    void testGet_NotFound() {
        when(opsRepository.findById(testOpsId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> opsService.get(testOpsId));
    }

    @Test
    void testList_Success() {
        var pageable = PageRequest.of(0, 10);
        when(opsRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(testOps)));

        opsService.list(pageable);

        verify(opsRepository, times(1)).findAll(pageable);
    }

    @Test
    void testFindByOrder_Success() {
        when(opsRepository.findByOrderIdOrderByStepOrderAsc(testOrderId)).thenReturn(List.of(testOps));

        opsService.findByOrder(testOrderId);

        verify(opsRepository, times(1)).findByOrderIdOrderByStepOrderAsc(testOrderId);
    }

    @Test
    void testDelete_Success() {
        when(opsRepository.existsById(testOpsId)).thenReturn(true);

        opsService.delete(testOpsId);

        verify(opsRepository, times(1)).deleteById(testOpsId);
    }

    @Test
    void testDelete_NotFound() {
        when(opsRepository.existsById(testOpsId)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> opsService.delete(testOpsId));
        verify(opsRepository, never()).deleteById(any());
    }

    @Test
    void testPatch_CancelledOrder_throws() {
        testOrder.setStatus(OrderStatus.CANCELLED);
        ProductionStepPatchRequest req = new ProductionStepPatchRequest();
        req.setStatus(ProductionStepStatus.IN_PROGRESS);

        when(opsRepository.findById(testOpsId)).thenReturn(Optional.of(testOps));

        assertThrows(IllegalStateException.class, () -> opsService.patch(testOpsId, req));
        verify(opsRepository, never()).save(any());
    }

    @Test
    void testPatch_NotLastStep_noProductionCompletion() {
        ProductionStepPatchRequest req = new ProductionStepPatchRequest();
        req.setStatus(ProductionStepStatus.DONE);

        OrderProductionStep siblingPending = new OrderProductionStep();
        siblingPending.setStatus(ProductionStepStatus.PENDING);

        when(opsRepository.findById(testOpsId)).thenReturn(Optional.of(testOps));
        when(opsRepository.save(testOps)).thenReturn(testOps);
        when(opsRepository.findByOrderItemIdOrderByStepOrderAsc(testOrderItemId))
                .thenReturn(List.of(testOps, siblingPending));

        opsService.patch(testOpsId, req);

        verify(stockService, never()).goodsReceipt(any());
        verify(stockService, never()).goodsIssue(any());
    }

    @Test
    void testPatch_LastStepDone_completesProductionAndConvertsReservations() {
        ProductionStepPatchRequest req = new ProductionStepPatchRequest();
        req.setStatus(ProductionStepStatus.DONE);
        UUID warehouseId = UUID.randomUUID();
        req.setWarehouseId(warehouseId);

        Item componentItem = new Item();
        componentItem.setId(UUID.randomUUID());
        BillOfMaterial bomLine = new BillOfMaterial();
        bomLine.setComponent(componentItem);
        bomLine.setQuantity(new BigDecimal("4"));

        Warehouse reservationWarehouse = new Warehouse();
        reservationWarehouse.setId(UUID.randomUUID());
        StockReservation reservation = new StockReservation();
        reservation.setItem(componentItem);
        reservation.setWarehouse(reservationWarehouse);
        reservation.setQuantity(4L);
        reservation.setStatus(ReservationStatus.ACTIVE);

        when(opsRepository.findById(testOpsId)).thenReturn(Optional.of(testOps));
        when(opsRepository.save(testOps)).thenReturn(testOps);
        when(opsRepository.findByOrderItemIdOrderByStepOrderAsc(testOrderItemId))
                .thenReturn(List.of(testOps));
        when(bomRepository.findByItemId(testProducedItem.getId())).thenReturn(List.of(bomLine));
        when(reservationRepository.findByItemIdAndOrderIdAndStatus(componentItem.getId(), testOrderId, ReservationStatus.ACTIVE))
                .thenReturn(List.of(reservation));

        opsService.patch(testOpsId, req);

        verify(stockService, times(1)).goodsReceipt(any());
        verify(stockService, times(1)).goodsIssue(any());
        assertEquals(ReservationStatus.FULFILLED, reservation.getStatus());
    }

    @Test
    void testPatch_LastStepDone_missingWarehouseId_throws() {
        ProductionStepPatchRequest req = new ProductionStepPatchRequest();
        req.setStatus(ProductionStepStatus.DONE);

        when(opsRepository.findById(testOpsId)).thenReturn(Optional.of(testOps));
        when(opsRepository.save(testOps)).thenReturn(testOps);
        when(opsRepository.findByOrderItemIdOrderByStepOrderAsc(testOrderItemId))
                .thenReturn(List.of(testOps));

        assertThrows(IllegalArgumentException.class, () -> opsService.patch(testOpsId, req));
        verify(stockService, never()).goodsReceipt(any());
    }
}

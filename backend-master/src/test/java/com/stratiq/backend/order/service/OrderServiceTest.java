package com.stratiq.backend.order.service;

import com.stratiq.backend.inventory.model.Item;
import com.stratiq.backend.inventory.repository.BillOfMaterialRepository;
import com.stratiq.backend.order.dto.request.OrderCreateRequest;
import com.stratiq.backend.order.mapper.OrderMapper;
import com.stratiq.backend.order.model.Customer;
import com.stratiq.backend.order.model.Order;
import com.stratiq.backend.order.model.OrderItem;
import com.stratiq.backend.order.model.OrderStatus;
import com.stratiq.backend.order.repository.CustomerRepository;
import com.stratiq.backend.order.repository.OrderItemRepository;
import com.stratiq.backend.order.repository.OrderRepository;
import com.stratiq.backend.shared.exception.NotFoundException;
import com.stratiq.backend.warehouse.model.ReservationStatus;
import com.stratiq.backend.warehouse.model.StockReservation;
import com.stratiq.backend.warehouse.repository.StockMovementRepository;
import com.stratiq.backend.warehouse.repository.StockReservationRepository;
import com.stratiq.backend.warehouse.service.StockReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private BillOfMaterialRepository bomRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private StockReservationRepository stockReservationRepository;

    @Mock
    private StockReservationService stockReservationService;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order testOrder;
    private Customer testCustomer;
    private UUID testOrderId;
    private UUID testCustomerId;

    @BeforeEach
    void setUp() {
        testOrderId = UUID.randomUUID();
        testCustomerId = UUID.randomUUID();

        testCustomer = new Customer();
        testCustomer.setId(testCustomerId);
        testCustomer.setName("Acme Corp");

        testOrder = new Order();
        testOrder.setId(testOrderId);
        testOrder.setCustomer(testCustomer);
        testOrder.setStatus(OrderStatus.DRAFT);
    }

    @Test
    void testCreateOrder_Success() {
        // Arrange
        OrderCreateRequest request = new OrderCreateRequest(
            testCustomerId, "ORD-001", OrderStatus.DRAFT, null,
            LocalDateTime.now(), LocalDateTime.now().plusDays(7),
            null, "Test order"
        );

        when(customerRepository.findById(testCustomerId)).thenReturn(Optional.of(testCustomer));
        when(orderMapper.toEntity(request, testCustomer)).thenReturn(testOrder);
        when(orderRepository.save(testOrder)).thenReturn(testOrder);

        // Act
        orderService.create(request);

        // Assert
        verify(customerRepository, times(1)).findById(testCustomerId);
        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    void testCreateOrder_CustomerNotFound() {
        // Arrange
        OrderCreateRequest request = new OrderCreateRequest(
            testCustomerId, "ORD-001", OrderStatus.DRAFT, null,
            LocalDateTime.now(), LocalDateTime.now().plusDays(7),
            null, "Test order"
        );

        when(customerRepository.findById(testCustomerId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> orderService.create(request));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testGetOrder_Success() {
        // Arrange
        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));

        // Act
        orderService.get(testOrderId);

        // Assert
        verify(orderRepository, times(1)).findById(testOrderId);
    }

    @Test
    void testGetOrder_NotFound() {
        // Arrange
        when(orderRepository.findById(testOrderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> orderService.get(testOrderId));
    }

    @Test
    void testGetOrdersByCustomer_Success() {
        // Arrange
        List<Order> orders = List.of(testOrder);
        when(orderRepository.findByCustomerId(testCustomerId)).thenReturn(orders);

        // Act
        orderService.findByCustomer(testCustomerId);

        // Assert
        verify(orderRepository, times(1)).findByCustomerId(testCustomerId);
    }

    @Test
    void testGetOrdersByStatus_Success() {
        // Arrange
        List<Order> orders = List.of(testOrder);
        when(orderRepository.findByStatus(OrderStatus.DRAFT)).thenReturn(orders);

        // Act
        orderService.findByStatus(OrderStatus.DRAFT);

        // Assert
        verify(orderRepository, times(1)).findByStatus(OrderStatus.DRAFT);
    }

    @Test
    void testListOrders_Success() {
        // Arrange
        var pageable = PageRequest.of(0, 10);
        when(orderRepository.findAll(pageable)).thenReturn(mock(org.springframework.data.domain.Page.class));

        // Act
        orderService.list(pageable);

        // Assert
        verify(orderRepository, times(1)).findAll(pageable);
    }

    @Test
    void testDeleteOrder_Success() {
        // Arrange
        when(orderRepository.existsById(testOrderId)).thenReturn(true);

        // Act
        orderService.delete(testOrderId);

        // Assert
        verify(orderRepository, times(1)).existsById(testOrderId);
        verify(orderRepository, times(1)).deleteById(testOrderId);
    }

    @Test
    void testDeleteOrder_NotFound() {
        // Arrange
        when(orderRepository.existsById(testOrderId)).thenReturn(false);

        // Act & Assert
        assertThrows(NotFoundException.class, () -> orderService.delete(testOrderId));
        verify(orderRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    void testConfirmOrder_NoBomDefined_justUpdatesStatus() {
        // Arrange: order line's item has no BOM, no reservation should be attempted.
        Item plainItem = new Item();
        plainItem.setId(UUID.randomUUID());

        OrderItem orderItem = new OrderItem();
        orderItem.setItem(plainItem);
        orderItem.setQuantity(new BigDecimal("3"));

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
        when(orderItemRepository.findByOrderId(testOrderId)).thenReturn(List.of(orderItem));
        when(bomRepository.findByItemId(plainItem.getId())).thenReturn(List.of());

        // Act
        orderService.confirm(testOrderId);

        // Assert
        assertEquals(OrderStatus.CONFIRMED, testOrder.getStatus());
        assertNotNull(testOrder.getConfirmedAt());
        verify(stockReservationService, never()).create(any());
    }

    @Test
    void testConfirmOrder_InsufficientStock_throws() {
        // Arrange: an item with a BOM whose component has zero stock anywhere.
        Item component = new Item();
        component.setId(UUID.randomUUID());

        Item ownProduct = new Item();
        ownProduct.setId(UUID.randomUUID());

        OrderItem orderItem = new OrderItem();
        orderItem.setId(UUID.randomUUID());
        orderItem.setItem(ownProduct);
        orderItem.setQuantity(BigDecimal.ONE);

        com.stratiq.backend.inventory.model.BillOfMaterial bomLine = new com.stratiq.backend.inventory.model.BillOfMaterial();
        bomLine.setComponent(component);
        bomLine.setQuantity(new BigDecimal("2"));

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
        when(orderItemRepository.findByOrderId(testOrderId)).thenReturn(List.of(orderItem));
        when(bomRepository.findByItemId(ownProduct.getId())).thenReturn(List.of(bomLine));
        when(stockMovementRepository.findStockByWarehouseForItem(component.getId())).thenReturn(List.of());

        // Act & Assert
        assertThrows(com.stratiq.backend.shared.exception.InsufficientStockException.class,
                () -> orderService.confirm(testOrderId));
        verify(stockReservationService, never()).create(any());
    }

    @Test
    void testCancelOrder_ReleasesActiveReservations() {
        // Arrange
        StockReservation reservation = new StockReservation();
        reservation.setId(UUID.randomUUID());

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
        when(stockReservationRepository.findByOrderIdAndStatus(testOrderId, ReservationStatus.ACTIVE))
                .thenReturn(List.of(reservation));

        // Act
        orderService.cancel(testOrderId);

        // Assert
        assertEquals(OrderStatus.CANCELLED, testOrder.getStatus());
        assertNotNull(testOrder.getCancelledAt());
        verify(stockReservationService, times(1)).release(reservation.getId());
    }
}

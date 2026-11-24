package com.stratiq.backend.order.service;

import com.stratiq.backend.inventory.model.Item;
import com.stratiq.backend.inventory.repository.ItemRepository;
import com.stratiq.backend.inventory.repository.SupplierRepository;
import com.stratiq.backend.order.dto.request.OrderItemCreateRequest;
import com.stratiq.backend.order.dto.request.OrderItemUpdateRequest;
import com.stratiq.backend.order.mapper.OrderItemMapper;
import com.stratiq.backend.order.model.Order;
import com.stratiq.backend.order.model.OrderItem;
import com.stratiq.backend.order.repository.OrderItemRepository;
import com.stratiq.backend.order.repository.OrderRepository;
import com.stratiq.backend.shared.exception.NotFoundException;
import com.stratiq.backend.shared.model.Unit;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private OrderItemMapper mapper;

    @InjectMocks
    private OrderItemServiceImpl orderItemService;

    private OrderItem testOrderItem;
    private Order testOrder;
    private Item testItem;
    private UUID testOrderItemId;
    private UUID testOrderId;
    private UUID testItemId;

    @BeforeEach
    void setUp() {
        testOrderItemId = UUID.randomUUID();
        testOrderId = UUID.randomUUID();
        testItemId = UUID.randomUUID();

        testOrder = new Order();
        testOrder.setId(testOrderId);

        testItem = new Item();
        testItem.setId(testItemId);
        testItem.setName("Widget A");

        testOrderItem = new OrderItem();
        testOrderItem.setId(testOrderItemId);
        testOrderItem.setOrder(testOrder);
        testOrderItem.setItem(testItem);
    }

    @Test
    void testCreate_Success() {
        OrderItemCreateRequest request = new OrderItemCreateRequest(
            testOrderId, testItemId, "Widget A", Unit.piece,
            new BigDecimal("5"), new BigDecimal("100.00"), null, null
        );

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
        when(itemRepository.findById(testItemId)).thenReturn(Optional.of(testItem));
        when(mapper.toEntity(any(), any(), any(), any())).thenReturn(testOrderItem);
        when(orderItemRepository.save(testOrderItem)).thenReturn(testOrderItem);

        orderItemService.create(request);

        // findById is called twice: once to validate the order, once when recalculating the order total.
        verify(orderRepository, times(2)).findById(testOrderId);
        verify(itemRepository, times(1)).findById(testItemId);
        verify(orderItemRepository, times(1)).save(testOrderItem);
    }

    @Test
    void testCreate_OrderNotFound() {
        OrderItemCreateRequest request = new OrderItemCreateRequest(
            testOrderId, testItemId, "Widget A", Unit.piece,
            new BigDecimal("5"), new BigDecimal("100.00"), null, null
        );

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> orderItemService.create(request));
        verify(orderItemRepository, never()).save(any());
    }

    @Test
    void testCreate_ItemNotFound() {
        OrderItemCreateRequest request = new OrderItemCreateRequest(
            testOrderId, testItemId, "Widget A", Unit.piece,
            new BigDecimal("5"), new BigDecimal("100.00"), null, null
        );

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
        when(itemRepository.findById(testItemId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> orderItemService.create(request));
        verify(orderItemRepository, never()).save(any());
    }

    @Test
    void testGet_Success() {
        when(orderItemRepository.findById(testOrderItemId)).thenReturn(Optional.of(testOrderItem));

        orderItemService.get(testOrderItemId);

        verify(orderItemRepository, times(1)).findById(testOrderItemId);
    }

    @Test
    void testGet_NotFound() {
        when(orderItemRepository.findById(testOrderItemId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> orderItemService.get(testOrderItemId));
    }

    @Test
    void testList_Success() {
        var pageable = PageRequest.of(0, 10);
        when(orderItemRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(testOrderItem)));

        orderItemService.list(pageable);

        verify(orderItemRepository, times(1)).findAll(pageable);
    }

    @Test
    void testFindByOrder_Success() {
        when(orderItemRepository.findByOrderId(testOrderId)).thenReturn(List.of(testOrderItem));

        orderItemService.findByOrder(testOrderId);

        verify(orderItemRepository, times(1)).findByOrderId(testOrderId);
    }

    @Test
    void testDelete_Success() {
        when(orderItemRepository.findById(testOrderItemId)).thenReturn(Optional.of(testOrderItem));

        orderItemService.delete(testOrderItemId);

        verify(orderItemRepository, times(1)).delete(testOrderItem);
    }

    @Test
    void testDelete_NotFound() {
        when(orderItemRepository.findById(testOrderItemId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> orderItemService.delete(testOrderItemId));
        verify(orderItemRepository, never()).delete(any());
    }
}

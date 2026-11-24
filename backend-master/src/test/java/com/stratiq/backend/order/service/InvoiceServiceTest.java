package com.stratiq.backend.order.service;

import com.stratiq.backend.order.dto.request.InvoiceCreateRequest;
import com.stratiq.backend.order.dto.request.InvoiceUpdateRequest;
import com.stratiq.backend.order.mapper.InvoiceMapper;
import com.stratiq.backend.order.model.Invoice;
import com.stratiq.backend.order.model.InvoiceStatus;
import com.stratiq.backend.order.model.Order;
import com.stratiq.backend.order.repository.InvoiceRepository;
import com.stratiq.backend.order.repository.OrderItemRepository;
import com.stratiq.backend.order.repository.OrderRepository;
import com.stratiq.backend.shared.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private InvoiceMapper mapper;

    @InjectMocks
    private InvoiceServiceImpl invoiceService;

    private Invoice testInvoice;
    private Order testOrder;
    private UUID testInvoiceId;
    private UUID testOrderId;

    @BeforeEach
    void setUp() {
        testInvoiceId = UUID.randomUUID();
        testOrderId = UUID.randomUUID();

        testOrder = new Order();
        testOrder.setId(testOrderId);

        testInvoice = new Invoice();
        testInvoice.setId(testInvoiceId);
        testInvoice.setInvoiceNumber("INV-001");
        testInvoice.setOrder(testOrder);
        testInvoice.setStatus(InvoiceStatus.ISSUED);

        ReflectionTestUtils.setField(invoiceService, "vatRate", new BigDecimal("0.19"));
    }

    @Test
    void testCreate_Success() {
        InvoiceCreateRequest request = new InvoiceCreateRequest(
            testOrderId, InvoiceStatus.ISSUED, null, null, null
        );

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
        when(orderItemRepository.findByOrderId(testOrderId)).thenReturn(List.of());
        when(invoiceRepository.findMaxSequenceForYear(anyInt())).thenReturn(null);
        when(mapper.toEntity(any(), any(), any(), any(), any(), any())).thenReturn(testInvoice);
        when(invoiceRepository.save(testInvoice)).thenReturn(testInvoice);

        invoiceService.create(request);

        verify(orderRepository, times(1)).findById(testOrderId);
        verify(invoiceRepository, times(1)).save(testInvoice);
    }

    @Test
    void testCreate_OrderNotFound() {
        InvoiceCreateRequest request = new InvoiceCreateRequest(
            testOrderId, InvoiceStatus.ISSUED, null, null, null
        );

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> invoiceService.create(request));
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void testGet_Success() {
        when(invoiceRepository.findById(testInvoiceId)).thenReturn(Optional.of(testInvoice));

        invoiceService.get(testInvoiceId);

        verify(invoiceRepository, times(1)).findById(testInvoiceId);
    }

    @Test
    void testGet_NotFound() {
        when(invoiceRepository.findById(testInvoiceId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> invoiceService.get(testInvoiceId));
    }

    @Test
    void testList_Success() {
        var pageable = PageRequest.of(0, 10);
        when(invoiceRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(testInvoice)));

        invoiceService.list(pageable);

        verify(invoiceRepository, times(1)).findAll(pageable);
    }

    @Test
    void testFindByOrder_Success() {
        when(invoiceRepository.findByOrderId(testOrderId)).thenReturn(List.of(testInvoice));

        invoiceService.findByOrder(testOrderId);

        verify(invoiceRepository, times(1)).findByOrderId(testOrderId);
    }

    @Test
    void testFindByStatus_Success() {
        when(invoiceRepository.findByStatus(InvoiceStatus.ISSUED)).thenReturn(List.of(testInvoice));

        invoiceService.findByStatus(InvoiceStatus.ISSUED);

        verify(invoiceRepository, times(1)).findByStatus(InvoiceStatus.ISSUED);
    }

    @Test
    void testUpdate_Success() {
        InvoiceUpdateRequest request = new InvoiceUpdateRequest(
            InvoiceStatus.PAID, null, null, null, null, null, null, null
        );

        when(invoiceRepository.findById(testInvoiceId)).thenReturn(Optional.of(testInvoice));

        invoiceService.update(testInvoiceId, request);

        verify(invoiceRepository, times(1)).findById(testInvoiceId);
        verify(mapper, times(1)).update(testInvoice, request);
    }

    @Test
    void testDelete_Success() {
        when(invoiceRepository.existsById(testInvoiceId)).thenReturn(true);

        invoiceService.delete(testInvoiceId);

        verify(invoiceRepository, times(1)).deleteById(testInvoiceId);
    }

    @Test
    void testDelete_NotFound() {
        when(invoiceRepository.existsById(testInvoiceId)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> invoiceService.delete(testInvoiceId));
        verify(invoiceRepository, never()).deleteById(any());
    }
}

package com.stratiq.backend.order.mapper;

import com.stratiq.backend.order.dto.request.InvoiceCreateRequest;
import com.stratiq.backend.order.dto.request.InvoiceUpdateRequest;
import com.stratiq.backend.order.dto.response.InvoiceResponse;
import com.stratiq.backend.order.model.Invoice;
import com.stratiq.backend.order.model.InvoiceStatus;
import com.stratiq.backend.order.model.Order;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceMapperTest {

    private final InvoiceMapper mapper = new InvoiceMapper();

    @Test
    void toEntity_withExplicitStatus_usesProvidedStatus() {
        Order order = new Order();
        order.setId(UUID.randomUUID());
        InvoiceCreateRequest req = new InvoiceCreateRequest(
                order.getId(), InvoiceStatus.PAID, null, null, null
        );

        Invoice result = mapper.toEntity(req, order, "INV-2026-0001",
                new BigDecimal("100"), new BigDecimal("19"), new BigDecimal("119"));

        assertThat(result.getInvoiceNumber()).isEqualTo("INV-2026-0001");
        assertThat(result.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(result.getSubtotal()).isEqualByComparingTo("100");
        assertThat(result.getOrder()).isEqualTo(order);
    }

    @Test
    void toEntity_withNullStatus_defaultsToIssued() {
        Order order = new Order();
        InvoiceCreateRequest req = new InvoiceCreateRequest(
                UUID.randomUUID(), null, null, null, null
        );

        Invoice result = mapper.toEntity(req, order, "INV-2026-0002",
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        assertThat(result.getStatus()).isEqualTo(InvoiceStatus.ISSUED);
    }

    @Test
    void toEntity_usesAmountsPassedIn() {
        Order order = new Order();
        InvoiceCreateRequest req = new InvoiceCreateRequest(
                UUID.randomUUID(), null, null, null, null
        );

        Invoice result = mapper.toEntity(req, order, "INV-2026-0003",
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        assertThat(result.getSubtotal()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getTaxAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void update_withNonNullFields_overwritesExisting() {
        Invoice invoice = new Invoice();
        invoice.setStatus(InvoiceStatus.ISSUED);
        invoice.setNotes("Old notes");
        InvoiceUpdateRequest req = new InvoiceUpdateRequest(
                InvoiceStatus.PAID, null, LocalDateTime.now(), null, null, null, null, "Paid on time"
        );

        mapper.update(invoice, req);

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(invoice.getNotes()).isEqualTo("Paid on time");
        assertThat(invoice.getPaidAt()).isNotNull();
    }

    @Test
    void update_withNullFields_keepsExistingValues() {
        Invoice invoice = new Invoice();
        invoice.setStatus(InvoiceStatus.ISSUED);
        invoice.setNotes("Keep me");
        InvoiceUpdateRequest req = new InvoiceUpdateRequest(
                null, null, null, null, null, null, null, null
        );

        mapper.update(invoice, req);

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.ISSUED);
        assertThat(invoice.getNotes()).isEqualTo("Keep me");
    }

    @Test
    void toResponse_mapsOrderId() {
        Order order = new Order();
        UUID orderId = UUID.randomUUID();
        order.setId(orderId);

        Invoice invoice = new Invoice();
        invoice.setId(UUID.randomUUID());
        invoice.setInvoiceNumber("INV-001");
        invoice.setOrder(order);
        invoice.setStatus(InvoiceStatus.ISSUED);
        invoice.setSubtotal(BigDecimal.ZERO);
        invoice.setTaxAmount(BigDecimal.ZERO);
        invoice.setTotalAmount(BigDecimal.ZERO);

        InvoiceResponse response = mapper.toResponse(invoice);

        assertThat(response.invoiceNumber()).isEqualTo("INV-001");
        assertThat(response.orderId()).isEqualTo(orderId);
        assertThat(response.status()).isEqualTo(InvoiceStatus.ISSUED);
    }
}

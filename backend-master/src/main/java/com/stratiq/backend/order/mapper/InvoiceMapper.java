package com.stratiq.backend.order.mapper;

import com.stratiq.backend.order.dto.request.InvoiceCreateRequest;
import com.stratiq.backend.order.dto.request.InvoiceUpdateRequest;
import com.stratiq.backend.order.dto.response.InvoiceResponse;
import com.stratiq.backend.order.model.Invoice;
import com.stratiq.backend.order.model.InvoiceStatus;
import com.stratiq.backend.order.model.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class InvoiceMapper {

    // invoiceNumber and the amounts are computed by the service (server-generated
    // sequential number; amounts derived from the order's line items), not taken from the request.
    public Invoice toEntity(InvoiceCreateRequest req, Order order, String invoiceNumber,
                             BigDecimal subtotal, BigDecimal taxAmount, BigDecimal totalAmount) {
        Invoice inv = new Invoice();
        inv.setInvoiceNumber(invoiceNumber);
        inv.setOrder(order);
        // Default status: ISSUED.
        inv.setStatus(req.status() != null ? req.status() : InvoiceStatus.ISSUED);
        inv.setIssueDate(req.issueDate());
        inv.setDueDate(req.dueDate());
        inv.setSubtotal(subtotal);
        inv.setTaxAmount(taxAmount);
        inv.setTotalAmount(totalAmount);
        inv.setNotes(req.notes());
        return inv;
    }

    public void update(Invoice entity, InvoiceUpdateRequest req) {
        // Selective update: only non-null fields are applied (supports status changes, payments, cancellations).
        if (req.status()      != null) entity.setStatus(req.status());
        if (req.dueDate()     != null) entity.setDueDate(req.dueDate());
        if (req.paidAt()      != null) entity.setPaidAt(req.paidAt());
        if (req.cancelledAt() != null) entity.setCancelledAt(req.cancelledAt());
        if (req.subtotal()    != null) entity.setSubtotal(req.subtotal());
        if (req.taxAmount()   != null) entity.setTaxAmount(req.taxAmount());
        if (req.totalAmount() != null) entity.setTotalAmount(req.totalAmount());
        if (req.notes()       != null) entity.setNotes(req.notes());
    }

    public InvoiceResponse toResponse(Invoice e) {
        return new InvoiceResponse(
                e.getId(), e.getCreatedAt(),
                e.getInvoiceNumber(), e.getOrder().getId(),
                e.getStatus(), e.getIssueDate(), e.getDueDate(),
                e.getPaidAt(), e.getCancelledAt(),
                e.getSubtotal(), e.getTaxAmount(), e.getTotalAmount(),
                e.getNotes()
        );
    }
}

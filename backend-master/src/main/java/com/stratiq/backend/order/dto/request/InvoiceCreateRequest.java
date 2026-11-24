package com.stratiq.backend.order.dto.request;

import com.stratiq.backend.order.model.InvoiceStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

// invoiceNumber and the subtotal/tax/total amounts are not client input:
// the invoice number is generated server-side (sequential per year, legally required)
// and the amounts are calculated from the order's line items.
public record InvoiceCreateRequest(
        @NotNull UUID orderId,
        InvoiceStatus status,
        LocalDateTime issueDate,
        LocalDateTime dueDate,
        String notes
) {}

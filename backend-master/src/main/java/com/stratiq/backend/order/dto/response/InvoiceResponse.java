package com.stratiq.backend.order.dto.response;

import com.stratiq.backend.order.model.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record InvoiceResponse(
        UUID id,
        LocalDateTime createdAt,
        String invoiceNumber,
        UUID orderId,
        InvoiceStatus status,
        LocalDateTime issueDate,
        LocalDateTime dueDate,
        LocalDateTime paidAt,
        LocalDateTime cancelledAt,
        BigDecimal subtotal,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        String notes
) {}

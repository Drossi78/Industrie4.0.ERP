package com.stratiq.backend.order.dto.request;

import com.stratiq.backend.order.model.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InvoiceUpdateRequest(
        InvoiceStatus status,
        LocalDateTime dueDate,
        LocalDateTime paidAt,
        LocalDateTime cancelledAt,
        BigDecimal subtotal,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        String notes
) {}

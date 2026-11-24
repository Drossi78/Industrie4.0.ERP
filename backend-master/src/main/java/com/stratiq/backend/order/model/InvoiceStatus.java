package com.stratiq.backend.order.model;

public enum InvoiceStatus {
    // Invoice lifecycle: ISSUED → PAID (or CANCELLED).
    ISSUED,     // Invoice generated and sent
    PAID,       // Payment received
    CANCELLED   // Invoice voided or reversed
}

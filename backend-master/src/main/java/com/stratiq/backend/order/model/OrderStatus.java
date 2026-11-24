package com.stratiq.backend.order.model;

public enum OrderStatus {
    // Order lifecycle: DRAFT → CONFIRMED → COMPLETED (or CANCELLED at any stage).
    DRAFT,      // Initial state, not yet confirmed
    CONFIRMED,  // Order verified and sent
    CANCELLED,  // Order rejected or cancelled
    COMPLETED   // Order fulfilled
}

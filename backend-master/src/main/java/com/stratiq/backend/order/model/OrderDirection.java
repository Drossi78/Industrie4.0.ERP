package com.stratiq.backend.order.model;

public enum OrderDirection {
    // Whether fulfilling this order books stock IN (goods receipt / procurement)
    // or OUT (goods issue / sale). Chosen at order creation.
    IN,
    OUT
}

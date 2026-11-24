package com.stratiq.backend.warehouse.model;

// Lifecycle of a stock reservation.
// ACTIVE: currently blocking available stock.
// RELEASED: cancelled without being consumed (e.g. order cancelled), no longer blocking.
// FULFILLED: consumed by production completion and converted into an actual stock-out.
public enum ReservationStatus {
    ACTIVE,
    RELEASED,
    FULFILLED
}

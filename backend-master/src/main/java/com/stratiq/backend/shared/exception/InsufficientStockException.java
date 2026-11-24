package com.stratiq.backend.shared.exception;

// Thrown when a goods issue or transfer requests more stock than is available
// in the source warehouse. Mapped to HTTP 409 Conflict.
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
}

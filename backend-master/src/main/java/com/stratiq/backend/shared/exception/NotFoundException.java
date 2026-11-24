package com.stratiq.backend.shared.exception;

public class NotFoundException extends RuntimeException {
    // Custom exception for missing entity lookups; returns HTTP 404 when thrown.
    public NotFoundException(String entity, Object id) {
        super("%s not found: %s".formatted(entity, id));
    }
}

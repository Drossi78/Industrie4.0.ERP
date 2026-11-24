package com.stratiq.backend.production.model;

public enum ProductionStepStatus {
    // Production step workflow: PENDING → IN_PROGRESS → DONE.
    PENDING,        // Step not started
    IN_PROGRESS,    // Step is being executed
    DONE            // Step completed
}

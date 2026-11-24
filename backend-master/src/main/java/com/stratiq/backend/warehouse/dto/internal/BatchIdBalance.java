package com.stratiq.backend.warehouse.dto.internal;

import java.util.UUID;

/**
 * Internal projection: derived balance (SUM(IN) - SUM(OUT)) keyed by batch id.
 * Used to enrich batch reads with their current stock without storing it.
 */
public record BatchIdBalance(UUID batchId, Long balance) {}

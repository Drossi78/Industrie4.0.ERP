package com.stratiq.backend.warehouse.dto.internal;

import com.stratiq.backend.inventory.model.Batch;

/**
 * Internal projection: the derived available balance of a single batch
 * (SUM(IN) - SUM(OUT) over its stock movements). Used to drive FEFO/FIFO
 * allocation during a goods issue. Not exposed via the API.
 */
public record BatchBalance(Batch batch, Long balance) {}

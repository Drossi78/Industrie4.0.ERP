package com.stratiq.backend.warehouse.dto.response;

import java.util.UUID;

// Current stock level for a single item, summed across all warehouses.
// balance = totalIn - totalOut
public record StockLevelResponse(
        UUID itemId,
        String itemName,
        Long totalIn,
        Long totalOut,
        Long balance
) {}

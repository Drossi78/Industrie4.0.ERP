package com.stratiq.backend.warehouse.dto.response;

import java.util.UUID;

// Stock level of one asset broken down per warehouse.
// balance = totalIn - totalOut for this warehouse.
public record WarehouseStockResponse(
        UUID warehouseId,
        String warehouseName,
        Long totalIn,
        Long totalOut,
        Long balance
) {}

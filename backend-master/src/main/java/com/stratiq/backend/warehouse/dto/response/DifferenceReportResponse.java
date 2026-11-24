package com.stratiq.backend.warehouse.dto.response;

import java.util.UUID;

public record DifferenceReportResponse(
        UUID itemId,
        String itemName,
        UUID warehouseId,
        String warehouseName,
        Long totalIn,
        Long totalOut,
        Long balance
) {}

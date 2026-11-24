package com.stratiq.backend.order.dto.request;

import com.stratiq.backend.shared.model.Unit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;
import com.stratiq.backend.shared.model.Unit;

public record OrderItemUpdateRequest(
        @Size(max = 255) String itemName,
        Unit unit,
        @DecimalMin("0.001") BigDecimal quantity,
        @DecimalMin("0.00") BigDecimal unitPrice,
        String notes,
        UUID supplierId
) {}

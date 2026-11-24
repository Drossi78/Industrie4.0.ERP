package com.stratiq.backend.order.dto.request;

import com.stratiq.backend.shared.model.Unit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;
import com.stratiq.backend.shared.model.Unit;

public record OrderItemCreateRequest(
        @NotNull UUID orderId,
        @NotNull UUID itemId,
        @NotBlank @Size(max = 255) String itemName,
        @NotNull Unit unit,
        @NotNull @DecimalMin("0.001") BigDecimal quantity,
        @NotNull @DecimalMin("0.00") BigDecimal unitPrice,
        String notes,
        UUID supplierId
) {}

package com.sqa.oms.order.modules.order.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record OrderItemRequest(String productId,
                               String productName,
                               @NotNull @Positive Integer quantity,
                               @NotNull @DecimalMin(value = "0.0") BigDecimal unitPrice) {
}

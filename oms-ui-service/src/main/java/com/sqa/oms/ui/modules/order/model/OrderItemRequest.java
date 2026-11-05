package com.sqa.oms.ui.modules.order.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Request body of a single order item.
 *
 * @param productId   referenced catalog product id, or null for a free-text line
 * @param productName display name used when no product is referenced
 * @param quantity    ordered quantity
 * @param unitPrice   price per unit
 */
public record OrderItemRequest(Long productId,
                               String productName,
                               @NotNull @Positive Integer quantity,
                               @NotNull @DecimalMin(value = "0.0") BigDecimal unitPrice) {
}

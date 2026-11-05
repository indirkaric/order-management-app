package com.sqa.oms.ui.modules.order.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Request body of the create product endpoint.
 *
 * @param name        product name
 * @param sku         stock keeping unit, must be unique
 * @param description free text description
 * @param unitPrice   catalog price
 */
public record CreateProductRequest(@NotBlank String name,
                                   @NotBlank String sku,
                                   String description,
                                   @NotNull @DecimalMin(value = "0.0") BigDecimal unitPrice) {
}

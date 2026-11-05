package com.sqa.oms.order.modules.product.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateProductRequest(@NotBlank String name,
                                   @NotBlank String sku,
                                   String description,
                                   @NotNull @DecimalMin(value = "0.0") BigDecimal unitPrice) {
}

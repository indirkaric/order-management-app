package com.sqa.oms.order.modules.product.repository.projection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Column level projection of the product table, selected directly by the Criteria API queries
 * instead of loading the full {@code Product} entity.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductProjection {

    private String entityId;

    private String name;

    private String sku;

    private String description;

    private BigDecimal unitPrice;
}

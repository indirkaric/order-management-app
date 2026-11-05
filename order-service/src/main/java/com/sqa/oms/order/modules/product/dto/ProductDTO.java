package com.sqa.oms.order.modules.product.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Service layer representation of a product, returned by {@code IProductService} to the
 * response creator services.
 */
@Data
public class ProductDTO {

    private String entityId;

    private String name;

    private String sku;

    private String description;

    private BigDecimal unitPrice;
}

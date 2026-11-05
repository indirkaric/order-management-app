package com.sqa.oms.order.modules.product.model;

import com.sqa.oms.order.annotation.Trimmed;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Filters of the product list endpoint.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductListRequest {

    @Trimmed
    private String sku;

    @Trimmed
    private String textSearch;
}

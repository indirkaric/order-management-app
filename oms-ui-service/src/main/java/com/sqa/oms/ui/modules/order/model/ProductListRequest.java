package com.sqa.oms.ui.modules.order.model;

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

    private String sku;

    private String textSearch;
}

package com.sqa.oms.order.modules.product.service;

import com.sqa.oms.order.modules.product.model.ProductResponse;

/**
 * Builds the response of the single product endpoint.
 */
public interface IProductResponseCreatorService {

    /**
     * Returns a single product.
     *
     * @param entityId business key of the product
     * @return ProductResponse
     */
    ProductResponse create(String entityId);
}

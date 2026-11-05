package com.sqa.oms.order.modules.product.service;

import com.sqa.oms.order.modules.product.model.CreateProductRequest;
import com.sqa.oms.order.modules.product.model.ProductResponse;

/**
 * Builds the response of the create product endpoint.
 */
public interface ICreateProductResponseCreatorService {

    /**
     * Creates a new catalog product and returns its full representation.
     *
     * @param request create request
     * @return ProductResponse
     */
    ProductResponse create(CreateProductRequest request);
}

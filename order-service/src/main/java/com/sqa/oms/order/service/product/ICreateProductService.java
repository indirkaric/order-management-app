package com.sqa.oms.order.service.product;

import com.sqa.oms.order.modules.product.model.CreateProductRequest;

/**
 * Creates catalog products.
 */
public interface ICreateProductService {

    /**
     * Saves a new catalog product.
     *
     * @param request the create request
     * @return the business key of the created product
     */
    String save(CreateProductRequest request);
}

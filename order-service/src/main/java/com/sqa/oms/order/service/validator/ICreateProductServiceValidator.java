package com.sqa.oms.order.service.validator;

import com.sqa.oms.order.modules.product.model.CreateProductRequest;

/**
 * Validates the product create flow.
 */
public interface ICreateProductServiceValidator {

    /**
     * Validates a create product request.
     *
     * @param request request to validate
     */
    void validate(CreateProductRequest request);
}

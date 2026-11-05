package com.sqa.oms.order.service.validator;

import com.sqa.oms.order.modules.order.model.CreateOrderRequest;

/**
 * Validates the order create flow.
 */
public interface ICreateOrderServiceValidator {

    /**
     * Validates a create order request.
     *
     * @param request request to validate
     */
    void validate(CreateOrderRequest request);
}

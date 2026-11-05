package com.sqa.oms.order.service.validator;

import com.sqa.oms.order.enums.OrderStatus;
import com.sqa.oms.order.modules.order.model.UpdateOrderRequest;

/**
 * Validates the order update flow.
 */
public interface IModifyOrderServiceValidator {

    /**
     * Validates an update order request.
     *
     * @param request request to validate
     */
    void validate(UpdateOrderRequest request);

    /**
     * Validates that the order may move from its current status to the requested one.
     *
     * @param current current status
     * @param target  requested status
     */
    void validateStatusTransition(OrderStatus current, OrderStatus target);
}

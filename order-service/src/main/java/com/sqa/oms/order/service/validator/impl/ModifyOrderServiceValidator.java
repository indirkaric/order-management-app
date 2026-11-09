package com.sqa.oms.order.service.validator.impl;

import com.sqa.oms.order.enums.OrderStatus;
import com.sqa.oms.order.exception.BusinessException;
import com.sqa.oms.order.modules.order.model.UpdateOrderRequest;
import com.sqa.oms.order.service.validator.BaseOrderDataValidator;
import com.sqa.oms.order.service.validator.IModifyOrderServiceValidator;
import com.sqa.oms.order.util.Check;
import org.springframework.stereotype.Service;

/**
 * Default implementation of the order update validation.
 */
@Service
public final class ModifyOrderServiceValidator extends BaseOrderDataValidator
        implements IModifyOrderServiceValidator {

    @Override
    public void validate(final UpdateOrderRequest request) {
        Check.isNotNull(request, "request");

        validateItems(request.items());
    }

    @Override
    public void validateStatusTransition(final OrderStatus current, final OrderStatus target) {
        if (current == null || target == null || !current.canTransitionTo(target)) {
            throw new BusinessException("Status transition from " + current + " to " + target + " is not allowed.");
        }
    }
}

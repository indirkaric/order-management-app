package com.sqa.oms.order.service.validator.impl;

import com.sqa.oms.order.modules.order.model.CreateOrderRequest;
import com.sqa.oms.order.service.validator.BaseOrderDataValidator;
import com.sqa.oms.order.service.validator.ICreateOrderServiceValidator;
import com.sqa.oms.order.util.Check;
import org.springframework.stereotype.Service;

/**
 * Default implementation of the order create validation.
 */
@Service
public final class CreateOrderServiceValidator extends BaseOrderDataValidator
        implements ICreateOrderServiceValidator {

    @Override
    public void validate(final CreateOrderRequest request) {
        Check.isNotNull(request, "request");

        validateItems(request.items());
    }
}

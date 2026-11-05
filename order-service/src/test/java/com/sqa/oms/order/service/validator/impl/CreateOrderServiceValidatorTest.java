package com.sqa.oms.order.service.validator.impl;

import com.sqa.oms.order.exception.BusinessException;
import com.sqa.oms.order.modules.order.model.CreateOrderRequest;
import com.sqa.oms.order.modules.order.model.OrderItemRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateOrderServiceValidatorTest {

    private final CreateOrderServiceValidator validator = new CreateOrderServiceValidator();

    @Test
    void shouldRejectRequestWithNoItems() {
        CreateOrderRequest request = new CreateOrderRequest(null, List.of());

        assertThatThrownBy(() -> validator.validate(request)).isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldRejectItemWithoutProductIdOrName() {
        CreateOrderRequest request = new CreateOrderRequest(null,
                List.of(new OrderItemRequest(null, null, 1, BigDecimal.TEN)));

        assertThatThrownBy(() -> validator.validate(request)).isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldAcceptAValidRequest() {
        CreateOrderRequest request = new CreateOrderRequest(null,
                List.of(new OrderItemRequest(null, "Keyboard", 1, BigDecimal.TEN)));

        assertThatCode(() -> validator.validate(request)).doesNotThrowAnyException();
    }
}

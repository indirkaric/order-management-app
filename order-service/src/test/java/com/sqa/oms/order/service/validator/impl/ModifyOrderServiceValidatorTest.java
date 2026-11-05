package com.sqa.oms.order.service.validator.impl;

import com.sqa.oms.order.enums.OrderStatus;
import com.sqa.oms.order.exception.BusinessException;
import com.sqa.oms.order.modules.order.model.OrderItemRequest;
import com.sqa.oms.order.modules.order.model.UpdateOrderRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ModifyOrderServiceValidatorTest {

    private final ModifyOrderServiceValidator validator = new ModifyOrderServiceValidator();

    @Test
    void shouldRejectRequestWithNoItems() {
        UpdateOrderRequest request = new UpdateOrderRequest(List.of());

        assertThatThrownBy(() -> validator.validate(request)).isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldAcceptAValidRequest() {
        UpdateOrderRequest request = new UpdateOrderRequest(
                List.of(new OrderItemRequest(null, "Keyboard", 1, BigDecimal.TEN)));

        assertThatCode(() -> validator.validate(request)).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectIllegalStatusTransition() {
        assertThatThrownBy(() -> validator.validateStatusTransition(OrderStatus.SHIPPED, OrderStatus.CREATED))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldAcceptLegalStatusTransition() {
        assertThatCode(() -> validator.validateStatusTransition(OrderStatus.CREATED, OrderStatus.CONFIRMED))
                .doesNotThrowAnyException();
    }
}

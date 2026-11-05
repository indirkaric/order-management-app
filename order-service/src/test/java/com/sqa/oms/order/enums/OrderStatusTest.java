package com.sqa.oms.order.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusTest {

    @Test
    void shouldAllowCreatedToMoveToConfirmedOrCancelled() {
        assertThat(OrderStatus.CREATED.canTransitionTo(OrderStatus.CONFIRMED)).isTrue();
        assertThat(OrderStatus.CREATED.canTransitionTo(OrderStatus.CANCELLED)).isTrue();
        assertThat(OrderStatus.CREATED.canTransitionTo(OrderStatus.SHIPPED)).isFalse();
    }

    @Test
    void shouldAllowConfirmedToMoveToShippedOrCancelled() {
        assertThat(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.SHIPPED)).isTrue();
        assertThat(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.CANCELLED)).isTrue();
        assertThat(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.CREATED)).isFalse();
    }

    @Test
    void shouldNotAllowTerminalStatusesToTransitionAnywhere() {
        assertThat(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.CANCELLED)).isFalse();
        assertThat(OrderStatus.CANCELLED.canTransitionTo(OrderStatus.CREATED)).isFalse();
    }

    @Test
    void shouldNeverAllowNullTarget() {
        assertThat(OrderStatus.CREATED.canTransitionTo(null)).isFalse();
    }
}

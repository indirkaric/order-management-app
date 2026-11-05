package com.sqa.oms.order.service.order.impl;

import com.sqa.oms.order.enums.OrderStatus;
import com.sqa.oms.order.exception.EntityNotFoundException;
import com.sqa.oms.order.modules.order.mapper.IOrderMapper;
import com.sqa.oms.order.modules.order.repository.IOrderRepository;
import com.sqa.oms.order.modules.order.repository.projection.OrderProjection;
import com.sqa.oms.order.service.security.ICallerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final int PAGE_SIZE = 10;

    @Mock
    private IOrderRepository orderRepository;

    @Mock
    private IOrderMapper orderMapper;

    @Mock
    private ICallerContext callerContext;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, orderMapper, callerContext);
    }

    @Test
    void shouldRestrictListOrdersToOwnCustomerIdWhenNotElevated() {
        when(callerContext.isElevated()).thenReturn(false);
        when(callerContext.getUserId()).thenReturn("caller-id");
        when(orderRepository.findPagedList("caller-id", null, PageRequest.of(0, PAGE_SIZE)))
                .thenReturn(Page.empty());

        orderService.getPagedList(null, PageRequest.of(0, PAGE_SIZE));

        verify(orderRepository).findPagedList("caller-id", null, PageRequest.of(0, PAGE_SIZE));
    }

    @Test
    void shouldPassNullCustomerIdToListOrdersWhenElevated() {
        when(callerContext.isElevated()).thenReturn(true);
        when(orderRepository.findPagedList(null, OrderStatus.CREATED, PageRequest.of(0, PAGE_SIZE)))
                .thenReturn(Page.empty());

        orderService.getPagedList(OrderStatus.CREATED, PageRequest.of(0, PAGE_SIZE));

        verify(orderRepository).findPagedList(null, OrderStatus.CREATED, PageRequest.of(0, PAGE_SIZE));
    }

    @Test
    void shouldThrowNotFoundForNonElevatedCallerSeeingSomeoneElsesOrder() {
        OrderProjection projection = new OrderProjection(
                "ORD-1", "ORD-00000001", "someone-else", OrderStatus.CREATED, BigDecimal.TEN, Instant.now());

        when(callerContext.isElevated()).thenReturn(false);
        when(callerContext.getUserId()).thenReturn("caller-id");
        when(orderRepository.findProjectionByEntityId("ORD-1")).thenReturn(Optional.of(projection));

        assertThatThrownBy(() -> orderService.getByEntityId("ORD-1")).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldThrowNotFoundWhenOrderDoesNotExist() {
        when(orderRepository.findProjectionByEntityId("ORD-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getByEntityId("ORD-1")).isInstanceOf(EntityNotFoundException.class);
    }
}

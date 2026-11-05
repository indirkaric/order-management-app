package com.sqa.oms.order.service.order.impl;

import com.sqa.oms.order.entity.Order;
import com.sqa.oms.order.enums.OrderStatus;
import com.sqa.oms.order.event.DomainChangeEvent;
import com.sqa.oms.order.exception.EntityNotFoundException;
import com.sqa.oms.order.modules.order.mapper.IOrderMapper;
import com.sqa.oms.order.modules.order.model.OrderResponse;
import com.sqa.oms.order.modules.order.repository.IOrderRepository;
import com.sqa.oms.order.service.security.ICallerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteOrderServiceTest {

    @Mock
    private IOrderRepository orderRepository;

    @Mock
    private IOrderMapper orderMapper;

    @Mock
    private ICallerContext callerContext;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private DeleteOrderService deleteOrderService;

    @BeforeEach
    void setUp() {
        deleteOrderService = new DeleteOrderService(orderRepository, orderMapper, callerContext,
                applicationEventPublisher);
    }

    @Test
    void shouldRaiseDeleteEventWhenOrderIsDeleted() {
        Order order = new Order();
        order.setId(1L);
        order.setEntityId("ORD-1");
        order.setCustomerId("caller-id");
        order.setOrderNumber("ORD-00000001");

        when(callerContext.isElevated()).thenReturn(true);
        when(callerContext.getUserId()).thenReturn("caller-id");
        when(orderRepository.findByEntityId("ORD-1")).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(mockOrderResponse());

        deleteOrderService.delete("ORD-1");

        verify(orderRepository).delete(order);
        verify(applicationEventPublisher).publishEvent(any(DomainChangeEvent.class));
    }

    @Test
    void shouldThrowNotFoundForNonElevatedCallerDeletingSomeoneElsesOrder() {
        Order order = new Order();
        order.setEntityId("ORD-1");
        order.setCustomerId("someone-else");

        when(callerContext.isElevated()).thenReturn(false);
        when(callerContext.getUserId()).thenReturn("caller-id");
        when(orderRepository.findByEntityId("ORD-1")).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> deleteOrderService.delete("ORD-1"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    private OrderResponse mockOrderResponse() {
        return new OrderResponse("ORD-1", "ORD-00000001", "caller-id", OrderStatus.CREATED,
                BigDecimal.ZERO, List.of(), null);
    }
}

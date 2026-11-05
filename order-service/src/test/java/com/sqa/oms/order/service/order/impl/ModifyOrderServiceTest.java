package com.sqa.oms.order.service.order.impl;

import com.sqa.oms.order.entity.Order;
import com.sqa.oms.order.enums.OrderStatus;
import com.sqa.oms.order.event.DomainChangeEvent;
import com.sqa.oms.order.exception.BusinessException;
import com.sqa.oms.order.exception.EntityNotFoundException;
import com.sqa.oms.order.modules.order.mapper.IOrderMapper;
import com.sqa.oms.order.modules.order.model.OrderItemRequest;
import com.sqa.oms.order.modules.order.model.OrderResponse;
import com.sqa.oms.order.modules.order.model.UpdateOrderRequest;
import com.sqa.oms.order.modules.order.model.UpdateOrderStatusRequest;
import com.sqa.oms.order.modules.order.repository.IOrderRepository;
import com.sqa.oms.order.modules.product.repository.IProductRepository;
import com.sqa.oms.order.service.security.ICallerContext;
import com.sqa.oms.order.service.validator.IModifyOrderServiceValidator;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModifyOrderServiceTest {

    @Mock
    private IOrderRepository orderRepository;

    @Mock
    private IProductRepository productRepository;

    @Mock
    private IOrderMapper orderMapper;

    @Mock
    private IModifyOrderServiceValidator validator;

    @Mock
    private ICallerContext callerContext;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private ModifyOrderService modifyOrderService;

    @BeforeEach
    void setUp() {
        modifyOrderService = new ModifyOrderService(orderRepository, productRepository, orderMapper,
                validator, callerContext, applicationEventPublisher);
    }

    @Test
    void shouldThrowNotFoundForNonElevatedCallerSeeingSomeoneElsesOrder() {
        Order order = new Order();
        order.setEntityId("ORD-1");
        order.setCustomerId("someone-else");

        UpdateOrderRequest request = new UpdateOrderRequest(
                List.of(new OrderItemRequest(null, "Keyboard", 1, BigDecimal.TEN)));

        when(callerContext.isElevated()).thenReturn(false);
        when(callerContext.getUserId()).thenReturn("caller-id");
        when(orderRepository.findByEntityId("ORD-1")).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> modifyOrderService.update("ORD-1", request))
                .isInstanceOf(EntityNotFoundException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldRejectIllegalStatusTransition() {
        Order order = new Order();
        order.setId(1L);
        order.setEntityId("ORD-1");
        order.setCustomerId("caller-id");
        order.setStatus(OrderStatus.SHIPPED);

        when(callerContext.isElevated()).thenReturn(true);
        when(orderRepository.findByEntityId("ORD-1")).thenReturn(Optional.of(order));
        doThrow(new BusinessException("illegal transition"))
                .when(validator).validateStatusTransition(OrderStatus.SHIPPED, OrderStatus.CREATED);

        assertThatThrownBy(() -> modifyOrderService.updateStatus("ORD-1",
                new UpdateOrderStatusRequest(OrderStatus.CREATED)))
                .isInstanceOf(BusinessException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldPersistAndRaiseEventWhenStatusIsUpdated() {
        Order order = new Order();
        order.setId(1L);
        order.setEntityId("ORD-1");
        order.setCustomerId("caller-id");
        order.setStatus(OrderStatus.CREATED);

        when(callerContext.isElevated()).thenReturn(true);
        when(callerContext.getUserId()).thenReturn("caller-id");
        when(orderRepository.findByEntityId("ORD-1")).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(mockOrderResponse());

        modifyOrderService.updateStatus("ORD-1", new UpdateOrderStatusRequest(OrderStatus.CONFIRMED));

        verify(orderRepository).save(order);
        verify(applicationEventPublisher).publishEvent(any(DomainChangeEvent.class));
    }

    private OrderResponse mockOrderResponse() {
        return new OrderResponse("ORD-1", "ORD-00000001", "caller-id", OrderStatus.CREATED,
                BigDecimal.ZERO, List.of(), null);
    }
}

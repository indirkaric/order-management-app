package com.sqa.oms.order.service.order.impl;

import com.sqa.oms.order.entity.Order;
import com.sqa.oms.order.entity.Product;
import com.sqa.oms.order.enums.OrderStatus;
import com.sqa.oms.order.event.DomainChangeEvent;
import com.sqa.oms.order.modules.order.mapper.IOrderMapper;
import com.sqa.oms.order.modules.order.model.CreateOrderRequest;
import com.sqa.oms.order.modules.order.model.OrderItemRequest;
import com.sqa.oms.order.modules.order.model.OrderResponse;
import com.sqa.oms.order.modules.order.repository.IOrderRepository;
import com.sqa.oms.order.modules.product.repository.IProductRepository;
import com.sqa.oms.order.service.security.ICallerContext;
import com.sqa.oms.order.service.validator.ICreateOrderServiceValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateOrderServiceTest {

    private static final String PRODUCT_ENTITY_ID = "PRD-5";

    private static final long PRODUCT_ID = 5L;

    @Mock
    private IOrderRepository orderRepository;

    @Mock
    private IProductRepository productRepository;

    @Mock
    private IOrderMapper orderMapper;

    @Mock
    private ICreateOrderServiceValidator validator;

    @Mock
    private ICallerContext callerContext;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private CreateOrderService createOrderService;

    @BeforeEach
    void setUp() {
        createOrderService = new CreateOrderService(orderRepository, productRepository, orderMapper,
                validator, callerContext, applicationEventPublisher);
    }

    @Test
    void shouldIgnoreRequestedCustomerIdWhenCallerIsNotElevated() {
        CreateOrderRequest request = new CreateOrderRequest("someone-else",
                List.of(new OrderItemRequest(null, "Keyboard", 1, BigDecimal.TEN)));

        when(callerContext.isElevated()).thenReturn(false);
        when(callerContext.getUserId()).thenReturn("caller-id");
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.toResponse(any(Order.class))).thenReturn(mockOrderResponse());

        createOrderService.save(request);

        verify(validator).validate(request);
        verify(applicationEventPublisher).publishEvent(any(DomainChangeEvent.class));

        org.mockito.ArgumentCaptor<Order> captor = org.mockito.ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        assertThat(captor.getValue().getCustomerId()).isEqualTo("caller-id");
    }

    @Test
    void shouldResolveProductReferenceAndComputeTotal() {
        Product product = new Product();
        product.setId(PRODUCT_ID);
        product.setEntityId(PRODUCT_ENTITY_ID);
        product.setName("Keyboard");

        CreateOrderRequest request = new CreateOrderRequest(null,
                List.of(new OrderItemRequest(PRODUCT_ENTITY_ID, null, 2, new BigDecimal("10.00"))));

        when(callerContext.isElevated()).thenReturn(true);
        when(callerContext.getUserId()).thenReturn("caller-id");
        when(productRepository.findByEntityId(PRODUCT_ENTITY_ID)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.toResponse(any(Order.class))).thenReturn(mockOrderResponse());

        createOrderService.save(request);

        org.mockito.ArgumentCaptor<Order> captor = org.mockito.ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        assertThat(captor.getValue().getTotalAmount()).isEqualTo(new BigDecimal("20.00"));
    }

    private OrderResponse mockOrderResponse() {
        return new OrderResponse("ORD-1", "ORD-00000001", "caller-id", OrderStatus.CREATED,
                BigDecimal.ZERO, List.of(), null);
    }
}

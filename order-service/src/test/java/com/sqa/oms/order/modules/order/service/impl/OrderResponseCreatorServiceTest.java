package com.sqa.oms.order.modules.order.service.impl;

import com.sqa.oms.order.enums.OrderStatus;
import com.sqa.oms.order.modules.order.dto.OrderDTO;
import com.sqa.oms.order.modules.order.mapper.IOrderMapper;
import com.sqa.oms.order.modules.order.model.OrderItemResponse;
import com.sqa.oms.order.modules.order.model.OrderResponse;
import com.sqa.oms.order.modules.order.repository.IOrderRepository;
import com.sqa.oms.order.modules.order.repository.projection.OrderItemProjection;
import com.sqa.oms.order.service.order.IOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderResponseCreatorServiceTest {

    @Mock
    private IOrderService orderService;

    @Mock
    private IOrderRepository orderRepository;

    @Mock
    private IOrderMapper orderMapper;

    private OrderResponseCreatorService responseCreatorService;

    @BeforeEach
    void setUp() {
        responseCreatorService = new OrderResponseCreatorService(orderService, orderRepository, orderMapper);
    }

    @Test
    void shouldFetchAndAttachTheOrderItems() {
        OrderDTO order = new OrderDTO();
        order.setEntityId("ORD-1");
        order.setStatus(OrderStatus.CREATED);
        order.setTotalAmount(BigDecimal.TEN);

        OrderItemProjection itemProjection = new OrderItemProjection(null, "Keyboard", 1, BigDecimal.TEN);
        List<OrderItemResponse> items = List.of(
                new OrderItemResponse(null, "Keyboard", 1, BigDecimal.TEN, BigDecimal.TEN));
        OrderResponse response = new OrderResponse("ORD-1", "ORD-00000001", "caller-id", OrderStatus.CREATED,
                BigDecimal.TEN, items, null);

        when(orderService.getByEntityId("ORD-1")).thenReturn(order);
        when(orderRepository.findItemProjectionsByOrderEntityId("ORD-1")).thenReturn(List.of(itemProjection));
        when(orderMapper.toItemProjectionResponseList(List.of(itemProjection))).thenReturn(items);
        when(orderMapper.toResponse(order, items)).thenReturn(response);

        OrderResponse result = responseCreatorService.create("ORD-1");

        assertThat(result).isEqualTo(response);
    }
}

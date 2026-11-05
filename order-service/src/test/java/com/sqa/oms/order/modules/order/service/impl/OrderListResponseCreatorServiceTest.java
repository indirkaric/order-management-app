package com.sqa.oms.order.modules.order.service.impl;

import com.sqa.oms.order.enums.OrderStatus;
import com.sqa.oms.order.modules.order.dto.OrderDTO;
import com.sqa.oms.order.modules.order.mapper.IOrderMapper;
import com.sqa.oms.order.modules.order.model.OrderListResponse;
import com.sqa.oms.order.modules.order.model.OrderResponse;
import com.sqa.oms.order.modules.order.repository.IOrderRepository;
import com.sqa.oms.order.service.model.PagedList;
import com.sqa.oms.order.service.order.IOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderListResponseCreatorServiceTest {

    private static final int PAGE_SIZE = 20;

    @Mock
    private IOrderService orderService;

    @Mock
    private IOrderRepository orderRepository;

    @Mock
    private IOrderMapper orderMapper;

    private OrderListResponseCreatorService responseCreatorService;

    @BeforeEach
    void setUp() {
        responseCreatorService = new OrderListResponseCreatorService(orderService, orderRepository, orderMapper);
    }

    @Test
    void shouldMapThePagedListIntoAListResponse() {
        OrderDTO order = new OrderDTO();
        order.setEntityId("ORD-1");
        order.setStatus(OrderStatus.CREATED);
        OrderResponse response = new OrderResponse("ORD-1", "ORD-00000001", "caller-id", OrderStatus.CREATED,
                BigDecimal.TEN, List.of(), null);

        when(orderService.getPagedList(OrderStatus.CREATED, PageRequest.of(0, PAGE_SIZE)))
                .thenReturn(new PagedList<>(List.of(order), 1));
        when(orderRepository.findItemProjectionsByOrderEntityId(anyString())).thenReturn(List.of());
        when(orderMapper.toItemProjectionResponseList(anyList())).thenReturn(List.of());
        when(orderMapper.toResponse(order, List.of())).thenReturn(response);

        OrderListResponse result = responseCreatorService.create(OrderStatus.CREATED, PageRequest.of(0, PAGE_SIZE));

        assertThat(result.getData()).containsExactly(response);
        assertThat(result.getCount()).isEqualTo(1);
    }
}

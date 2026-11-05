package com.sqa.oms.order.modules.order.service.impl;

import com.sqa.oms.order.enums.OrderStatus;
import com.sqa.oms.order.modules.order.model.CreateOrderRequest;
import com.sqa.oms.order.modules.order.model.OrderItemRequest;
import com.sqa.oms.order.modules.order.model.OrderResponse;
import com.sqa.oms.order.modules.order.service.IOrderResponseCreatorService;
import com.sqa.oms.order.service.order.ICreateOrderService;
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
class CreateOrderResponseCreatorServiceTest {

    @Mock
    private ICreateOrderService createOrderService;

    @Mock
    private IOrderResponseCreatorService orderResponseCreatorService;

    private CreateOrderResponseCreatorService responseCreatorService;

    @BeforeEach
    void setUp() {
        responseCreatorService = new CreateOrderResponseCreatorService(createOrderService,
                orderResponseCreatorService);
    }

    @Test
    void shouldSaveThenBuildTheResponseFromTheNewEntityId() {
        CreateOrderRequest request = new CreateOrderRequest(null,
                List.of(new OrderItemRequest(null, "Keyboard", 1, BigDecimal.TEN)));
        OrderResponse response = new OrderResponse("ORD-1", "ORD-00000001", "caller-id", OrderStatus.CREATED,
                BigDecimal.TEN, List.of(), null);

        when(createOrderService.save(request)).thenReturn("ORD-1");
        when(orderResponseCreatorService.create("ORD-1")).thenReturn(response);

        OrderResponse result = responseCreatorService.create(request);

        assertThat(result).isEqualTo(response);
    }
}

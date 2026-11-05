package com.sqa.oms.order.modules.order.service.impl;

import com.sqa.oms.order.enums.OrderStatus;
import com.sqa.oms.order.modules.order.model.OrderItemRequest;
import com.sqa.oms.order.modules.order.model.OrderResponse;
import com.sqa.oms.order.modules.order.model.UpdateOrderRequest;
import com.sqa.oms.order.modules.order.model.UpdateOrderStatusRequest;
import com.sqa.oms.order.modules.order.service.IOrderResponseCreatorService;
import com.sqa.oms.order.service.order.IModifyOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModifyOrderResponseCreatorServiceTest {

    @Mock
    private IModifyOrderService modifyOrderService;

    @Mock
    private IOrderResponseCreatorService orderResponseCreatorService;

    private ModifyOrderResponseCreatorService responseCreatorService;

    @BeforeEach
    void setUp() {
        responseCreatorService = new ModifyOrderResponseCreatorService(modifyOrderService,
                orderResponseCreatorService);
    }

    @Test
    void shouldUpdateThenBuildTheResponseFromTheEntityId() {
        UpdateOrderRequest request = new UpdateOrderRequest(
                List.of(new OrderItemRequest(null, "Keyboard", 1, BigDecimal.TEN)));
        OrderResponse response = mockResponse();

        when(orderResponseCreatorService.create("ORD-1")).thenReturn(response);

        OrderResponse result = responseCreatorService.update("ORD-1", request);

        verify(modifyOrderService).update("ORD-1", request);
        assertThat(result).isEqualTo(response);
    }

    @Test
    void shouldUpdateStatusThenBuildTheResponseFromTheEntityId() {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.CONFIRMED);
        OrderResponse response = mockResponse();

        when(orderResponseCreatorService.create("ORD-1")).thenReturn(response);

        OrderResponse result = responseCreatorService.updateStatus("ORD-1", request);

        verify(modifyOrderService).updateStatus("ORD-1", request);
        assertThat(result).isEqualTo(response);
    }

    private OrderResponse mockResponse() {
        return new OrderResponse("ORD-1", "ORD-00000001", "caller-id", OrderStatus.CREATED,
                BigDecimal.TEN, List.of(), null);
    }
}

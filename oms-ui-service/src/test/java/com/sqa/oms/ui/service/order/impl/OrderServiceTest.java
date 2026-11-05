package com.sqa.oms.ui.service.order.impl;

import com.sqa.oms.ui.client.order.OrderClient;
import com.sqa.oms.ui.client.response.ClientResponseWrapper;
import com.sqa.oms.ui.modules.order.model.CreateOrderRequest;
import com.sqa.oms.ui.modules.order.model.OrderItemRequest;
import com.sqa.oms.ui.modules.order.model.OrderListResponse;
import com.sqa.oms.ui.modules.order.model.OrderResponse;
import com.sqa.oms.ui.modules.order.model.OrderStatus;
import com.sqa.oms.ui.modules.order.model.UpdateOrderRequest;
import com.sqa.oms.ui.modules.order.model.UpdateOrderStatusRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final int PAGE_SIZE = 20;

    @Mock
    private OrderClient orderClient;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderClient);
    }

    @Test
    void shouldForwardTheQueryParamsAndMapTheBodyWhenGettingAll() {
        OrderListResponse response = new OrderListResponse();
        response.setData(List.of());

        when(orderClient.getOrders(any(), any())).thenReturn(new ClientResponseWrapper(HttpStatus.OK, response));

        assertThat(orderService.getAll(OrderStatus.CREATED, PageRequest.of(0, PAGE_SIZE))).isEqualTo(response);
    }

    @Test
    void shouldReturnAnEmptyResponseWhenTheOrderServiceReturnsNoContentOnGettingAll() {
        when(orderClient.getOrders(any(), any())).thenReturn(new ClientResponseWrapper(HttpStatus.NO_CONTENT, null));

        assertThat(orderService.getAll(OrderStatus.CREATED, PageRequest.of(0, PAGE_SIZE)).getData()).isEmpty();
    }

    @Test
    void shouldForwardTheRequestAndReturnTheCreatedOrderOnCreate() {
        CreateOrderRequest request = new CreateOrderRequest(null,
                List.of(new OrderItemRequest(null, "Keyboard", 1, BigDecimal.TEN)));
        OrderResponse response = mockResponse();

        when(orderClient.createOrder(request)).thenReturn(new ClientResponseWrapper(HttpStatus.CREATED, response));

        assertThat(orderService.create(request)).isEqualTo(response);
    }

    @Test
    void shouldReturnTheMappedOrderWhenGettingById() {
        OrderResponse response = mockResponse();

        when(orderClient.getOrder("1")).thenReturn(new ClientResponseWrapper(HttpStatus.OK, response));

        assertThat(orderService.getById("1")).isEqualTo(response);
    }

    @Test
    void shouldForwardTheRequestOnUpdate() {
        UpdateOrderRequest request = new UpdateOrderRequest(
                List.of(new OrderItemRequest(null, "Keyboard", 1, BigDecimal.TEN)));
        OrderResponse response = mockResponse();

        when(orderClient.updateOrder("1", request)).thenReturn(new ClientResponseWrapper(HttpStatus.OK, response));

        assertThat(orderService.update("1", request)).isEqualTo(response);
    }

    @Test
    void shouldForwardTheRequestOnUpdateStatus() {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.CONFIRMED);
        OrderResponse response = mockResponse();

        when(orderClient.updateOrderStatus("1", request))
                .thenReturn(new ClientResponseWrapper(HttpStatus.OK, response));

        assertThat(orderService.updateStatus("1", request)).isEqualTo(response);
    }

    @Test
    void shouldForwardTheNoContentResponseOnDelete() {
        when(orderClient.deleteOrder("1")).thenReturn(new ClientResponseWrapper(HttpStatus.NO_CONTENT, null));

        orderService.delete("1");

        verify(orderClient).deleteOrder("1");
    }

    private OrderResponse mockResponse() {
        return new OrderResponse(1L, "ORD-00000001", "caller-id", OrderStatus.CREATED,
                BigDecimal.TEN, List.of(), null);
    }
}

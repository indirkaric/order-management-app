package com.sqa.oms.ui.modules.order;

import com.sqa.oms.ui.modules.order.model.CreateOrderRequest;
import com.sqa.oms.ui.modules.order.model.OrderItemRequest;
import com.sqa.oms.ui.modules.order.model.OrderListResponse;
import com.sqa.oms.ui.modules.order.model.OrderResponse;
import com.sqa.oms.ui.modules.order.model.OrderStatus;
import com.sqa.oms.ui.modules.order.model.UpdateOrderRequest;
import com.sqa.oms.ui.modules.order.model.UpdateOrderStatusRequest;
import com.sqa.oms.ui.service.order.IOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    private static final int PAGE_SIZE = 20;

    @Mock
    private IOrderService orderService;

    private OrderController orderController;

    @BeforeEach
    void setUp() {
        orderController = new OrderController(orderService);
    }

    @Test
    void shouldForwardTheStatusFilterWhenGettingAll() {
        OrderListResponse response = new OrderListResponse(List.of(mockResponse()), 1);
        when(orderService.getAll(OrderStatus.CREATED, PageRequest.of(0, PAGE_SIZE))).thenReturn(response);

        ResponseEntity<OrderListResponse> result =
                orderController.getAll(OrderStatus.CREATED, PageRequest.of(0, PAGE_SIZE));

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void shouldReturnNoContentWhenNoOrdersAreFound() {
        OrderListResponse response = new OrderListResponse(List.of(), 0);
        when(orderService.getAll(OrderStatus.CREATED, PageRequest.of(0, PAGE_SIZE))).thenReturn(response);

        ResponseEntity<OrderListResponse> result =
                orderController.getAll(OrderStatus.CREATED, PageRequest.of(0, PAGE_SIZE));

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void shouldReturn201OnCreate() {
        CreateOrderRequest request = new CreateOrderRequest(null,
                List.of(new OrderItemRequest(null, "Keyboard", 1, BigDecimal.TEN)));
        OrderResponse response = mockResponse();
        when(orderService.create(request)).thenReturn(response);

        ResponseEntity<OrderResponse> result = orderController.create(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void shouldReturn204OnDelete() {
        ResponseEntity<Void> result = orderController.delete("1");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(orderService).delete("1");
    }

    @Test
    void shouldDelegateToTheServiceWhenUpdatingStatus() {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.CONFIRMED);
        OrderResponse response = mockResponse();
        when(orderService.updateStatus("1", request)).thenReturn(response);

        orderController.updateStatus("1", request);

        verify(orderService).updateStatus("1", request);
    }

    @Test
    void shouldDelegateToTheServiceWhenUpdating() {
        UpdateOrderRequest request = new UpdateOrderRequest(
                List.of(new OrderItemRequest(null, "Keyboard", 1, BigDecimal.TEN)));
        OrderResponse response = mockResponse();
        when(orderService.update("1", request)).thenReturn(response);

        orderController.update("1", request);

        verify(orderService).update("1", request);
    }

    private OrderResponse mockResponse() {
        return new OrderResponse(1L, "ORD-00000001", "caller-id", OrderStatus.CREATED,
                BigDecimal.TEN, List.of(), null);
    }
}

package com.sqa.oms.order.modules.order;

import com.sqa.oms.order.enums.OrderStatus;
import com.sqa.oms.order.modules.order.model.CreateOrderRequest;
import com.sqa.oms.order.modules.order.model.OrderItemRequest;
import com.sqa.oms.order.modules.order.model.OrderListResponse;
import com.sqa.oms.order.modules.order.model.OrderResponse;
import com.sqa.oms.order.modules.order.model.UpdateOrderRequest;
import com.sqa.oms.order.modules.order.model.UpdateOrderStatusRequest;
import com.sqa.oms.order.modules.order.service.ICreateOrderResponseCreatorService;
import com.sqa.oms.order.modules.order.service.IModifyOrderResponseCreatorService;
import com.sqa.oms.order.modules.order.service.IOrderListResponseCreatorService;
import com.sqa.oms.order.modules.order.service.IOrderResponseCreatorService;
import com.sqa.oms.order.service.order.IDeleteOrderService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    private static final int PAGE_SIZE = 20;

    @Mock
    private ICreateOrderResponseCreatorService createOrderResponseCreatorService;

    @Mock
    private IModifyOrderResponseCreatorService modifyOrderResponseCreatorService;

    @Mock
    private IDeleteOrderService deleteOrderService;

    @Mock
    private IOrderResponseCreatorService orderResponseCreatorService;

    @Mock
    private IOrderListResponseCreatorService orderListResponseCreatorService;

    private OrderController orderController;

    @BeforeEach
    void setUp() {
        orderController = new OrderController(createOrderResponseCreatorService, modifyOrderResponseCreatorService,
                deleteOrderService, orderResponseCreatorService, orderListResponseCreatorService);
    }

    @Test
    void shouldReturn201WithTheCreatedOrder() {
        CreateOrderRequest request = new CreateOrderRequest(null,
                List.of(new OrderItemRequest(null, "Keyboard", 1, BigDecimal.TEN)));
        OrderResponse response = mockResponse();
        when(createOrderResponseCreatorService.create(request)).thenReturn(response);

        ResponseEntity<OrderResponse> result = orderController.createOrder(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void shouldDelegateListOrdersToTheResponseCreator() {
        OrderListResponse response = new OrderListResponse(List.of(mockResponse()), 1L);
        when(orderListResponseCreatorService.create(eq(OrderStatus.CREATED), any())).thenReturn(response);

        ResponseEntity<OrderListResponse> result =
                orderController.listOrders(OrderStatus.CREATED, PageRequest.of(0, PAGE_SIZE));

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void shouldReturnTheResponseCreatorResponseForGetOrder() {
        OrderResponse response = mockResponse();
        when(orderResponseCreatorService.create("ORD-1")).thenReturn(response);

        ResponseEntity<OrderResponse> result = orderController.getOrder("ORD-1");

        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void shouldDelegateUpdateOrderToTheResponseCreator() {
        UpdateOrderRequest request = new UpdateOrderRequest(
                List.of(new OrderItemRequest(null, "Keyboard", 1, BigDecimal.TEN)));
        when(modifyOrderResponseCreatorService.update("ORD-1", request)).thenReturn(mockResponse());

        ResponseEntity<OrderResponse> result = orderController.updateOrder("ORD-1", request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(modifyOrderResponseCreatorService).update("ORD-1", request);
    }

    @Test
    void shouldDelegateUpdateOrderStatusToTheResponseCreator() {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.CONFIRMED);
        when(modifyOrderResponseCreatorService.updateStatus("ORD-1", request)).thenReturn(mockResponse());

        orderController.updateOrderStatus("ORD-1", request);

        verify(modifyOrderResponseCreatorService).updateStatus("ORD-1", request);
    }

    @Test
    void shouldReturn204WhenOrderIsDeleted() {
        ResponseEntity<Void> result = orderController.deleteOrder("ORD-1");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(deleteOrderService).delete("ORD-1");
    }

    private OrderResponse mockResponse() {
        return new OrderResponse("ORD-1", "ORD-00000001", "caller-id", OrderStatus.CREATED,
                BigDecimal.TEN, List.of(), null);
    }
}

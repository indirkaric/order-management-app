package com.sqa.oms.ui.service.order.impl;

import com.sqa.oms.ui.client.order.OrderClient;
import com.sqa.oms.ui.constants.Constants;
import com.sqa.oms.ui.modules.order.model.CreateOrderRequest;
import com.sqa.oms.ui.modules.order.model.OrderListResponse;
import com.sqa.oms.ui.modules.order.model.OrderResponse;
import com.sqa.oms.ui.modules.order.model.OrderStatus;
import com.sqa.oms.ui.modules.order.model.UpdateOrderRequest;
import com.sqa.oms.ui.modules.order.model.UpdateOrderStatusRequest;
import com.sqa.oms.ui.service.order.IOrderService;
import com.sqa.oms.ui.util.Check;
import com.sqa.oms.ui.util.ResponseUtil;
import lombok.SneakyThrows;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Forwards every order call to the order service.
 */
@Service
public class OrderService implements IOrderService {

    private final OrderClient orderClient;

    /**
     * Constructor with dependency injection.
     *
     * @param orderClient order service client
     */
    public OrderService(final OrderClient orderClient) {
        Check.isNotNull(orderClient, "orderClient");
        this.orderClient = orderClient;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SneakyThrows
    public OrderListResponse getAll(final OrderStatus status, final Pageable pageable) {
        Check.isNotNull(pageable, "pageable");

        OrderListResponse response = ResponseUtil.handleResponse(orderClient.getOrders(status, pageable),
                OrderListResponse.class, Constants.ORDER_SERVICE_NAME);

        return response == null ? new OrderListResponse() : response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SneakyThrows
    public OrderResponse getById(final String orderId) {
        Check.isNotEmpty(orderId, "orderId");
        return ResponseUtil.handleResponse(orderClient.getOrder(orderId), OrderResponse.class,
                Constants.ORDER_SERVICE_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SneakyThrows
    public OrderResponse create(final CreateOrderRequest request) {
        Check.isNotNull(request, "request");
        return ResponseUtil.handleResponse(orderClient.createOrder(request), OrderResponse.class,
                Constants.ORDER_SERVICE_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SneakyThrows
    public OrderResponse update(final String orderId, final UpdateOrderRequest request) {
        Check.isNotEmpty(orderId, "orderId");
        Check.isNotNull(request, "request");
        return ResponseUtil.handleResponse(orderClient.updateOrder(orderId, request), OrderResponse.class,
                Constants.ORDER_SERVICE_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SneakyThrows
    public OrderResponse updateStatus(final String orderId, final UpdateOrderStatusRequest request) {
        Check.isNotEmpty(orderId, "orderId");
        Check.isNotNull(request, "request");
        return ResponseUtil.handleResponse(orderClient.updateOrderStatus(orderId, request), OrderResponse.class,
                Constants.ORDER_SERVICE_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SneakyThrows
    public void delete(final String orderId) {
        Check.isNotEmpty(orderId, "orderId");
        ResponseUtil.handleResponse(orderClient.deleteOrder(orderId), Void.class, Constants.ORDER_SERVICE_NAME);
    }
}

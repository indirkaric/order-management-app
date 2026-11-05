package com.sqa.oms.order.modules.order.service;

import com.sqa.oms.order.modules.order.model.CreateOrderRequest;
import com.sqa.oms.order.modules.order.model.OrderResponse;

/**
 * Builds the response of the create order endpoint.
 */
public interface ICreateOrderResponseCreatorService {

    /**
     * Creates a new order for the resolved customer and returns its full representation.
     *
     * @param request create request
     * @return OrderResponse
     */
    OrderResponse create(CreateOrderRequest request);
}

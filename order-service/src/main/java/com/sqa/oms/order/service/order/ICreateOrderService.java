package com.sqa.oms.order.service.order;

import com.sqa.oms.order.modules.order.model.CreateOrderRequest;

/**
 * Creates orders.
 */
public interface ICreateOrderService {

    /**
     * Saves a new order for the resolved customer.
     *
     * @param request the create request
     * @return the business key of the created order
     */
    String save(CreateOrderRequest request);
}

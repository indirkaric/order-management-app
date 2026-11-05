package com.sqa.oms.order.modules.order.service;

import com.sqa.oms.order.modules.order.model.OrderResponse;
import com.sqa.oms.order.modules.order.model.UpdateOrderRequest;
import com.sqa.oms.order.modules.order.model.UpdateOrderStatusRequest;

/**
 * Builds the response of the update order and update order status endpoints.
 */
public interface IModifyOrderResponseCreatorService {

    /**
     * Replaces the items of an existing order and returns its full representation.
     *
     * @param entityId business key of the order
     * @param request  update request
     * @return OrderResponse
     */
    OrderResponse update(String entityId, UpdateOrderRequest request);

    /**
     * Moves an order to a new status and returns its full representation.
     *
     * @param entityId business key of the order
     * @param request  requested status
     * @return OrderResponse
     */
    OrderResponse updateStatus(String entityId, UpdateOrderStatusRequest request);
}

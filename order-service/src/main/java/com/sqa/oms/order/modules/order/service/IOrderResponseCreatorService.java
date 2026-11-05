package com.sqa.oms.order.modules.order.service;

import com.sqa.oms.order.modules.order.model.OrderResponse;

/**
 * Builds the response of the single order endpoint.
 */
public interface IOrderResponseCreatorService {

    /**
     * Returns a single order visible to the caller, with its items attached.
     *
     * @param entityId business key of the order
     * @return OrderResponse
     */
    OrderResponse create(String entityId);
}

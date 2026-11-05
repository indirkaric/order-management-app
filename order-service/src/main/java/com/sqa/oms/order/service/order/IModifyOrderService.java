package com.sqa.oms.order.service.order;

import com.sqa.oms.order.modules.order.model.UpdateOrderRequest;
import com.sqa.oms.order.modules.order.model.UpdateOrderStatusRequest;

/**
 * Updates orders.
 */
public interface IModifyOrderService {

    /**
     * Replaces the items of an existing order and recomputes its total.
     *
     * @param entityId business key of the order
     * @param request  update request
     */
    void update(String entityId, UpdateOrderRequest request);

    /**
     * Moves an order to a new status when the transition is legal.
     *
     * @param entityId business key of the order
     * @param request  requested status
     */
    void updateStatus(String entityId, UpdateOrderStatusRequest request);
}

package com.sqa.oms.order.service.order;

/**
 * Deletes orders.
 */
public interface IDeleteOrderService {

    /**
     * Deletes an order visible to the caller.
     *
     * @param entityId business key of the order
     */
    void delete(String entityId);
}

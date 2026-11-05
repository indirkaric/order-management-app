package com.sqa.oms.ui.service.order;

import com.sqa.oms.ui.modules.order.model.CreateOrderRequest;
import com.sqa.oms.ui.modules.order.model.OrderListResponse;
import com.sqa.oms.ui.modules.order.model.OrderResponse;
import com.sqa.oms.ui.modules.order.model.OrderStatus;
import com.sqa.oms.ui.modules.order.model.UpdateOrderRequest;
import com.sqa.oms.ui.modules.order.model.UpdateOrderStatusRequest;
import org.springframework.data.domain.Pageable;

/**
 * Proxy to the order resources of the order service.
 */
public interface IOrderService {

    /**
     * Retrieves the order list.
     *
     * @param status   optional status filter
     * @param pageable paging information
     * @return order list
     */
    OrderListResponse getAll(OrderStatus status, Pageable pageable);

    /**
     * Retrieves a single order.
     *
     * @param orderId order identifier
     * @return order
     */
    OrderResponse getById(String orderId);

    /**
     * Creates an order.
     *
     * @param request request body
     * @return created order
     */
    OrderResponse create(CreateOrderRequest request);

    /**
     * Updates an order.
     *
     * @param orderId order identifier
     * @param request request body
     * @return updated order
     */
    OrderResponse update(String orderId, UpdateOrderRequest request);

    /**
     * Updates the status of an order.
     *
     * @param orderId order identifier
     * @param request request body
     * @return updated order
     */
    OrderResponse updateStatus(String orderId, UpdateOrderStatusRequest request);

    /**
     * Deletes an order.
     *
     * @param orderId order identifier
     */
    void delete(String orderId);
}

package com.sqa.oms.order.modules.order.service;

import com.sqa.oms.order.enums.OrderStatus;
import com.sqa.oms.order.modules.order.model.OrderListResponse;
import org.springframework.data.domain.Pageable;

/**
 * Builds the response of the order list endpoint.
 */
public interface IOrderListResponseCreatorService {

    /**
     * Returns the paged list of orders visible to the caller, optionally filtered by status.
     *
     * @param status   optional status filter
     * @param pageable paging information
     * @return OrderListResponse
     */
    OrderListResponse create(OrderStatus status, Pageable pageable);
}

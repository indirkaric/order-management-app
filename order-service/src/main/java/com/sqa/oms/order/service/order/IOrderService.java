package com.sqa.oms.order.service.order;

import com.sqa.oms.order.enums.OrderStatus;
import com.sqa.oms.order.modules.order.dto.OrderDTO;
import com.sqa.oms.order.service.model.PagedList;
import org.springframework.data.domain.Pageable;

/**
 * Read access to orders.
 */
public interface IOrderService {

    /**
     * Returns a single order visible to the caller.
     *
     * @param entityId business key of the order
     * @return the order
     */
    OrderDTO getByEntityId(String entityId);

    /**
     * Returns the paged list of orders visible to the caller.
     *
     * @param status   optional status filter
     * @param pageable paging information
     * @return the matching paged list of orders
     */
    PagedList<OrderDTO> getPagedList(OrderStatus status, Pageable pageable);
}

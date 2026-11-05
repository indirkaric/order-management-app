package com.sqa.oms.order.modules.order.repository;

import com.sqa.oms.order.entity.Order;
import com.sqa.oms.order.enums.OrderStatus;
import com.sqa.oms.order.modules.order.repository.projection.OrderItemProjection;
import com.sqa.oms.order.modules.order.repository.projection.OrderProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface IOrderRepository {

    /**
     * Persists an order together with its items.
     *
     * @param order order to persist
     * @return the persisted order
     */
    Order save(Order order);

    /**
     * Looks an order up by its business key.
     *
     * @param entityId business key
     * @return the order when it exists
     */
    Optional<Order> findByEntityId(String entityId);

    /**
     * Looks the projection of an order up by its business key.
     *
     * @param entityId business key
     * @return the order projection when it exists
     */
    Optional<OrderProjection> findProjectionByEntityId(String entityId);

    /**
     * Returns a page of order projections, optionally narrowed to one customer and/or one status.
     *
     * @param customerId customer to restrict to, or null for all customers
     * @param status     status to filter by, or null for all statuses
     * @param pageable   paging information
     * @return page of order projections
     */
    Page<OrderProjection> findPagedList(String customerId, OrderStatus status, Pageable pageable);

    /**
     * Returns the item projections of an order, oldest first.
     *
     * @param orderEntityId business key of the order
     * @return the matching item projections
     */
    List<OrderItemProjection> findItemProjectionsByOrderEntityId(String orderEntityId);

    /**
     * Checks whether a business key is already taken.
     *
     * @param entityId business key
     * @return true when the key is taken
     */
    boolean existsByEntityId(String entityId);

    /**
     * Removes an order and its items.
     *
     * @param order order to remove
     */
    void delete(Order order);
}

package com.sqa.oms.order.modules.order.service.impl;

import com.sqa.oms.order.modules.order.dto.OrderDTO;
import com.sqa.oms.order.modules.order.mapper.IOrderMapper;
import com.sqa.oms.order.modules.order.model.OrderItemResponse;
import com.sqa.oms.order.modules.order.model.OrderResponse;
import com.sqa.oms.order.modules.order.repository.IOrderRepository;
import com.sqa.oms.order.modules.order.service.IOrderResponseCreatorService;
import com.sqa.oms.order.service.order.IOrderService;
import com.sqa.oms.order.util.Check;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Default implementation of the single order response builder.
 */
@Service
public final class OrderResponseCreatorService implements IOrderResponseCreatorService {

    private final IOrderService orderService;

    private final IOrderRepository orderRepository;

    private final IOrderMapper orderMapper;

    /**
     * Constructor with dependency injection.
     *
     * @param orderService    core order read access
     * @param orderRepository access to the order table, used to fetch the order's items
     * @param orderMapper     order dto and projection mapper
     */
    public OrderResponseCreatorService(final IOrderService orderService,
                                       final IOrderRepository orderRepository,
                                       final IOrderMapper orderMapper) {
        Check.isNotNull(orderService, "orderService");
        Check.isNotNull(orderRepository, "orderRepository");
        Check.isNotNull(orderMapper, "orderMapper");

        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
    }

    @Override
    public OrderResponse create(final String entityId) {
        Check.isNotEmpty(entityId, "entityId");

        return buildResponse(orderService.getByEntityId(entityId));
    }

    private OrderResponse buildResponse(final OrderDTO order) {
        List<OrderItemResponse> items = orderMapper.toItemProjectionResponseList(
                orderRepository.findItemProjectionsByOrderEntityId(order.getEntityId()));

        return orderMapper.toResponse(order, items);
    }
}

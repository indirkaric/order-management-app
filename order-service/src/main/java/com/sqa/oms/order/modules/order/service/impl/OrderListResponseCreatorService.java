package com.sqa.oms.order.modules.order.service.impl;

import com.sqa.oms.order.enums.OrderStatus;
import com.sqa.oms.order.modules.order.dto.OrderDTO;
import com.sqa.oms.order.modules.order.mapper.IOrderMapper;
import com.sqa.oms.order.modules.order.model.OrderItemResponse;
import com.sqa.oms.order.modules.order.model.OrderListResponse;
import com.sqa.oms.order.modules.order.model.OrderResponse;
import com.sqa.oms.order.modules.order.repository.IOrderRepository;
import com.sqa.oms.order.modules.order.service.IOrderListResponseCreatorService;
import com.sqa.oms.order.service.model.PagedList;
import com.sqa.oms.order.service.order.IOrderService;
import com.sqa.oms.order.util.Check;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Default implementation of the order list response builder.
 */
@Service
public class OrderListResponseCreatorService implements IOrderListResponseCreatorService {

    private final IOrderService orderService;

    private final IOrderRepository orderRepository;

    private final IOrderMapper orderMapper;

    /**
     * Constructor with dependency injection.
     *
     * @param orderService    core order read access
     * @param orderRepository access to the order table, used to fetch each order's items
     * @param orderMapper     order dto and projection mapper
     */
    public OrderListResponseCreatorService(final IOrderService orderService,
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
    public OrderListResponse create(final OrderStatus status, final Pageable pageable) {
        Check.isNotNull(pageable, "pageable");

        PagedList<OrderDTO> pagedList = orderService.getPagedList(status, pageable);
        List<OrderResponse> data = pagedList.getItems().stream().map(this::buildResponse).toList();

        return new OrderListResponse(data, pagedList.getCount());
    }

    private OrderResponse buildResponse(final OrderDTO order) {
        List<OrderItemResponse> items = orderMapper.toItemProjectionResponseList(
                orderRepository.findItemProjectionsByOrderEntityId(order.getEntityId()));

        return orderMapper.toResponse(order, items);
    }
}

package com.sqa.oms.order.service.order.impl;

import com.sqa.oms.order.enums.OrderStatus;
import com.sqa.oms.order.exception.BusinessException;
import com.sqa.oms.order.exception.EntityNotFoundException;
import com.sqa.oms.order.modules.order.dto.OrderDTO;
import com.sqa.oms.order.modules.order.mapper.IOrderMapper;
import com.sqa.oms.order.modules.order.repository.IOrderRepository;
import com.sqa.oms.order.modules.order.repository.projection.OrderProjection;
import com.sqa.oms.order.service.model.PagedList;
import com.sqa.oms.order.service.order.IOrderService;
import com.sqa.oms.order.service.security.ICallerContext;
import com.sqa.oms.order.util.Check;
import com.sqa.oms.order.util.StringHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Default implementation of the order read access.
 */
@Service
@Transactional(readOnly = true)
public class OrderService implements IOrderService {

    private final IOrderRepository orderRepository;

    private final IOrderMapper orderMapper;

    private final ICallerContext callerContext;

    /**
     * Constructor with dependency injection.
     *
     * @param orderRepository order persistence
     * @param orderMapper     order projection mapper
     * @param callerContext   resolved caller identity
     */
    public OrderService(final IOrderRepository orderRepository,
                        final IOrderMapper orderMapper,
                        final ICallerContext callerContext) {
        Check.isNotNull(orderRepository, "orderRepository");
        Check.isNotNull(orderMapper, "orderMapper");
        Check.isNotNull(callerContext, "callerContext");

        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.callerContext = callerContext;
    }

    @Override
    public OrderDTO getByEntityId(final String entityId) {
        Check.isNotEmpty(entityId, "entityId");

        return orderMapper.toDto(loadVisibleOrderProjection(entityId));
    }

    @Override
    public PagedList<OrderDTO> getPagedList(final OrderStatus status, final Pageable pageable) {
        Check.isNotNull(pageable, "pageable");

        // Elevated callers see every order; everybody else is pinned to their own customerId.
        String customerId = callerContext.isElevated() ? null : requireCallerId();

        Page<OrderProjection> page = orderRepository.findPagedList(customerId, status, pageable);
        List<OrderDTO> items = page.getContent().stream().map(orderMapper::toDto).toList();

        return new PagedList<>(items, page.getTotalElements());
    }

    private OrderProjection loadVisibleOrderProjection(final String entityId) {
        Optional<OrderProjection> order = orderRepository.findProjectionByEntityId(entityId);

        OrderProjection projection = order.orElseThrow(
                () -> new EntityNotFoundException("Order " + entityId + " was not found."));

        // A non elevated caller asking for somebody else's order gets 404 rather than 403, so the
        // existence of other customers' orders is never leaked. entityId alone is already unique,
        // so ownership is checked here instead of being folded into the repository query.
        if (!callerContext.isElevated() && !projection.getCustomerId().equals(requireCallerId())) {
            throw new EntityNotFoundException("Order " + entityId + " was not found.");
        }

        return projection;
    }

    private String requireCallerId() {
        String userId = callerContext.getUserId();

        if (StringHelper.isEmpty(userId)) {
            throw new BusinessException("The caller identity could not be resolved.");
        }

        return userId;
    }
}

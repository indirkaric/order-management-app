package com.sqa.oms.order.service.order.impl;

import com.sqa.oms.common.events.EntityType;
import com.sqa.oms.common.events.EventAction;
import com.sqa.oms.order.entity.Order;
import com.sqa.oms.order.event.DomainChangeEvent;
import com.sqa.oms.order.exception.BusinessException;
import com.sqa.oms.order.exception.EntityNotFoundException;
import com.sqa.oms.order.modules.order.mapper.IOrderMapper;
import com.sqa.oms.order.modules.order.model.OrderResponse;
import com.sqa.oms.order.modules.order.repository.IOrderRepository;
import com.sqa.oms.order.service.order.IDeleteOrderService;
import com.sqa.oms.order.service.security.ICallerContext;
import com.sqa.oms.order.util.Check;
import com.sqa.oms.order.util.StringHelper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of the order delete flow.
 */
@Service
@Transactional
public class DeleteOrderService implements IDeleteOrderService {

    private final IOrderRepository orderRepository;

    private final IOrderMapper orderMapper;

    private final ICallerContext callerContext;

    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Constructor with dependency injection.
     *
     * @param orderRepository           order persistence
     * @param orderMapper               entity to response mapper, used for the event payload
     * @param callerContext             resolved caller identity
     * @param applicationEventPublisher local event publisher
     */
    public DeleteOrderService(final IOrderRepository orderRepository,
                              final IOrderMapper orderMapper,
                              final ICallerContext callerContext,
                              final ApplicationEventPublisher applicationEventPublisher) {

        Check.isNotNull(orderRepository, "orderRepository");
        Check.isNotNull(orderMapper, "orderMapper");
        Check.isNotNull(callerContext, "callerContext");
        Check.isNotNull(applicationEventPublisher, "applicationEventPublisher");

        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.callerContext = callerContext;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * Deletes an order.
     *
     * @param entityId entityId of the order to delete
     */
    @Override
    public void delete(final String entityId) {
        Check.isNotEmpty(entityId, "entityId");

        Order order = loadVisibleOrder(entityId);
        OrderResponse response = orderMapper.toResponse(order);

        orderRepository.delete(order);

        raiseEvent(order.getEntityId(), response);
    }

    private Order loadVisibleOrder(final String entityId) {
        Order order = orderRepository.findByEntityId(entityId)
                .orElseThrow(() -> new EntityNotFoundException("Order " + entityId + " was not found."));

        // A non elevated caller deleting somebody else's order gets 404 rather than 403, so the
        // existence of other customers' orders is never leaked. entityId alone is already unique,
        // so ownership is checked here instead of being folded into the repository query.
        if (!callerContext.isElevated() && !order.getCustomerId().equals(requireCallerId())) {
            throw new EntityNotFoundException("Order " + entityId + " was not found.");
        }

        return order;
    }

    private void raiseEvent(final String entityId, final OrderResponse payload) {
        // Only a local Spring event is raised inside the transaction; the actual Kafka send is done
        // by the AFTER_COMMIT listener so that a rolled back change is never published.
        applicationEventPublisher.publishEvent(
                new DomainChangeEvent(EntityType.ORDER, EventAction.DELETE, entityId,
                        callerContext.getUserId(), payload));
    }

    private String requireCallerId() {
        String userId = callerContext.getUserId();

        if (StringHelper.isEmpty(userId)) {
            throw new BusinessException("The caller identity could not be resolved.");
        }

        return userId;
    }
}

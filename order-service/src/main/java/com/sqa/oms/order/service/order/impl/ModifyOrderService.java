package com.sqa.oms.order.service.order.impl;

import com.sqa.oms.common.events.EntityType;
import com.sqa.oms.common.events.EventAction;
import com.sqa.oms.order.entity.Order;
import com.sqa.oms.order.entity.OrderItem;
import com.sqa.oms.order.entity.Product;
import com.sqa.oms.order.event.DomainChangeEvent;
import com.sqa.oms.order.exception.BusinessException;
import com.sqa.oms.order.exception.EntityNotFoundException;
import com.sqa.oms.order.modules.order.mapper.IOrderMapper;
import com.sqa.oms.order.modules.order.mapper.OrderAmountCalculator;
import com.sqa.oms.order.modules.order.model.OrderItemRequest;
import com.sqa.oms.order.modules.order.model.UpdateOrderRequest;
import com.sqa.oms.order.modules.order.model.UpdateOrderStatusRequest;
import com.sqa.oms.order.modules.order.repository.IOrderRepository;
import com.sqa.oms.order.modules.product.repository.IProductRepository;
import com.sqa.oms.order.service.order.IModifyOrderService;
import com.sqa.oms.order.service.security.ICallerContext;
import com.sqa.oms.order.service.validator.IModifyOrderServiceValidator;
import com.sqa.oms.order.util.Check;
import com.sqa.oms.order.util.StringHelper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Default implementation of the order update flow.
 */
@Service
@Transactional
public class ModifyOrderService implements IModifyOrderService {

    private final IOrderRepository orderRepository;

    private final IProductRepository productRepository;

    private final IOrderMapper orderMapper;

    private final IModifyOrderServiceValidator validator;

    private final ICallerContext callerContext;

    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Constructor with dependency injection.
     *
     * @param orderRepository           order persistence
     * @param productRepository        catalog lookups for referenced products
     * @param orderMapper               entity to response mapper, used for the event payload
     * @param validator                 validator of the modify request
     * @param callerContext             resolved caller identity
     * @param applicationEventPublisher local event publisher
     */
    public ModifyOrderService(final IOrderRepository orderRepository,
                              final IProductRepository productRepository,
                              final IOrderMapper orderMapper,
                              final IModifyOrderServiceValidator validator,
                              final ICallerContext callerContext,
                              final ApplicationEventPublisher applicationEventPublisher) {

        Check.isNotNull(orderRepository, "orderRepository");
        Check.isNotNull(productRepository, "productRepository");
        Check.isNotNull(orderMapper, "orderMapper");
        Check.isNotNull(validator, "validator");
        Check.isNotNull(callerContext, "callerContext");
        Check.isNotNull(applicationEventPublisher, "applicationEventPublisher");

        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.orderMapper = orderMapper;
        this.validator = validator;
        this.callerContext = callerContext;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * Updates the items and details of an existing order.
     *
     * @param entityId entityId of the order to update
     * @param request  the new order content
     */
    @Override
    public void update(final String entityId, final UpdateOrderRequest request) {
        Check.isNotEmpty(entityId, "entityId");
        Check.isNotNull(request, "request");

        validator.validate(request);

        Order order = loadVisibleOrder(entityId);
        applyItems(order, request.items());

        Order saved = orderRepository.save(order);
        raiseEvent(saved);
    }

    /**
     * Moves an order to a new status.
     *
     * @param entityId entityId of the order to transition
     * @param request  the requested target status
     */
    @Override
    public void updateStatus(final String entityId, final UpdateOrderStatusRequest request) {
        Check.isNotEmpty(entityId, "entityId");
        Check.isNotNull(request, "request");

        Order order = loadVisibleOrder(entityId);
        validator.validateStatusTransition(order.getStatus(), request.status());
        order.setStatus(request.status());

        Order saved = orderRepository.save(order);
        raiseEvent(saved);
    }

    private Order loadVisibleOrder(final String entityId) {
        Order order = orderRepository.findByEntityId(entityId)
                .orElseThrow(() -> new EntityNotFoundException("Order " + entityId + " was not found."));

        requireVisible(order);

        return order;
    }

    private void requireVisible(final Order order) {
        // A non elevated caller touching somebody else's order gets 404 rather than 403, so the
        // existence of other customers' orders is never leaked. entityId alone is already unique,
        // so ownership is checked here instead of being folded into the repository query.
        if (!callerContext.isElevated() && !order.getCustomerId().equals(requireCallerId())) {
            throw new EntityNotFoundException("Order " + order.getEntityId() + " was not found.");
        }
    }

    private void applyItems(final Order order, final List<OrderItemRequest> itemRequests) {
        order.getItems().clear();

        for (OrderItemRequest itemRequest : itemRequests) {
            order.getItems().add(buildItem(order, itemRequest));
        }

        // The total is always recomputed from the items; a client supplied total is never trusted.
        order.setTotalAmount(OrderAmountCalculator.total(order.getItems()));
    }

    private OrderItem buildItem(final Order order, final OrderItemRequest itemRequest) {
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setQuantity(itemRequest.quantity());
        item.setUnitPrice(itemRequest.unitPrice());

        if (itemRequest.productId() == null) {
            item.setProductName(itemRequest.productName());

            return item;
        }

        Product product = productRepository.findByEntityId(itemRequest.productId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Product " + itemRequest.productId() + " was not found."));

        item.setProduct(product);
        item.setProductName(product.getName());

        return item;
    }

    private void raiseEvent(final Order order) {
        // Only a local Spring event is raised inside the transaction; the actual Kafka send is done
        // by the AFTER_COMMIT listener so that a rolled back change is never published.
        applicationEventPublisher.publishEvent(new DomainChangeEvent(
                EntityType.ORDER, EventAction.UPDATE, order.getEntityId(), callerContext.getUserId(),
                orderMapper.toResponse(order)));
    }

    private String requireCallerId() {
        String userId = callerContext.getUserId();

        if (StringHelper.isEmpty(userId)) {
            throw new BusinessException("The caller identity could not be resolved.");
        }

        return userId;
    }
}

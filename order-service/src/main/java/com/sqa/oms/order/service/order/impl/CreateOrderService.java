package com.sqa.oms.order.service.order.impl;

import com.sqa.oms.common.events.EntityType;
import com.sqa.oms.common.events.EventAction;
import com.sqa.oms.order.constants.Constants;
import com.sqa.oms.order.entity.Order;
import com.sqa.oms.order.entity.OrderItem;
import com.sqa.oms.order.entity.Product;
import com.sqa.oms.order.enums.OrderStatus;
import com.sqa.oms.order.event.DomainChangeEvent;
import com.sqa.oms.order.exception.BusinessException;
import com.sqa.oms.order.exception.EntityNotFoundException;
import com.sqa.oms.order.modules.order.mapper.IOrderMapper;
import com.sqa.oms.order.modules.order.mapper.OrderAmountCalculator;
import com.sqa.oms.order.modules.order.model.CreateOrderRequest;
import com.sqa.oms.order.modules.order.model.OrderItemRequest;
import com.sqa.oms.order.modules.order.repository.IOrderRepository;
import com.sqa.oms.order.modules.product.repository.IProductRepository;
import com.sqa.oms.order.service.order.ICreateOrderService;
import com.sqa.oms.order.service.security.ICallerContext;
import com.sqa.oms.order.service.validator.ICreateOrderServiceValidator;
import com.sqa.oms.order.util.Check;
import com.sqa.oms.order.util.EntityIdGenerator;
import com.sqa.oms.order.util.StringHelper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Default implementation of the order create flow.
 */
@Service
@Transactional
public class CreateOrderService implements ICreateOrderService {

    private final IOrderRepository orderRepository;

    private final IProductRepository productRepository;

    private final IOrderMapper orderMapper;

    private final ICreateOrderServiceValidator validator;

    private final ICallerContext callerContext;

    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Constructor with dependency injection.
     *
     * @param orderRepository           order persistence
     * @param productRepository        catalog lookups for referenced products
     * @param orderMapper               entity to response mapper, used for the event payload
     * @param validator                 validator of the create request
     * @param callerContext             resolved caller identity
     * @param applicationEventPublisher local event publisher
     */
    public CreateOrderService(final IOrderRepository orderRepository,
                              final IProductRepository productRepository,
                              final IOrderMapper orderMapper,
                              final ICreateOrderServiceValidator validator,
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

    @Override
    public String save(final CreateOrderRequest request) {
        Check.isNotNull(request, "request");

        validator.validate(request);

        String entityId = generateOrderEntityId();

        Order order = new Order();
        order.setEntityId(entityId);
        order.setCustomerId(resolveCustomerId(request.customerId()));
        order.setStatus(OrderStatus.CREATED);
        order.setTotalAmount(BigDecimal.ZERO);
        order.setOrderNumber(Constants.ORDER_NUMBER_PREFIX + entityId);

        applyItems(order, request.items());

        Order saved = orderRepository.save(order);
        raiseEvent(saved);

        return saved.getEntityId();
    }

    private String generateOrderEntityId() {
        String candidate = EntityIdGenerator.generate();
        while (orderRepository.existsByEntityId(candidate)) {
            candidate = EntityIdGenerator.generate();
        }

        return candidate;
    }

    private String resolveCustomerId(final String requestedCustomerId) {
        String callerId = requireCallerId();

        // A non elevated caller may only ever order for itself, so a customerId coming from the
        // request body is deliberately ignored.
        if (!callerContext.isElevated()) {
            return callerId;
        }

        return StringHelper.isEmpty(requestedCustomerId) ? callerId : requestedCustomerId;
    }

    private void applyItems(final Order order, final List<OrderItemRequest> itemRequests) {
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
                EntityType.ORDER, EventAction.CREATE, order.getEntityId(), callerContext.getUserId(),
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

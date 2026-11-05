package com.sqa.oms.order.modules.order.service.impl;

import com.sqa.oms.order.modules.order.model.CreateOrderRequest;
import com.sqa.oms.order.modules.order.model.OrderResponse;
import com.sqa.oms.order.modules.order.service.ICreateOrderResponseCreatorService;
import com.sqa.oms.order.modules.order.service.IOrderResponseCreatorService;
import com.sqa.oms.order.service.order.ICreateOrderService;
import com.sqa.oms.order.util.Check;
import org.springframework.stereotype.Service;

/**
 * Default implementation of the create order response builder.
 */
@Service
public class CreateOrderResponseCreatorService implements ICreateOrderResponseCreatorService {

    private final ICreateOrderService createOrderService;

    private final IOrderResponseCreatorService orderResponseCreatorService;

    /**
     * Constructor with dependency injection.
     *
     * @param createOrderService          core order create flow
     * @param orderResponseCreatorService builds the full response off the newly created entityId
     */
    public CreateOrderResponseCreatorService(final ICreateOrderService createOrderService,
                                             final IOrderResponseCreatorService orderResponseCreatorService) {
        Check.isNotNull(createOrderService, "createOrderService");
        Check.isNotNull(orderResponseCreatorService, "orderResponseCreatorService");

        this.createOrderService = createOrderService;
        this.orderResponseCreatorService = orderResponseCreatorService;
    }

    @Override
    public OrderResponse create(final CreateOrderRequest request) {
        Check.isNotNull(request, "request");

        String entityId = createOrderService.save(request);

        return orderResponseCreatorService.create(entityId);
    }
}

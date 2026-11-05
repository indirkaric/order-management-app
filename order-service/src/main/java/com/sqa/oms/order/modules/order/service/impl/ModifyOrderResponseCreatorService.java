package com.sqa.oms.order.modules.order.service.impl;

import com.sqa.oms.order.modules.order.model.OrderResponse;
import com.sqa.oms.order.modules.order.model.UpdateOrderRequest;
import com.sqa.oms.order.modules.order.model.UpdateOrderStatusRequest;
import com.sqa.oms.order.modules.order.service.IModifyOrderResponseCreatorService;
import com.sqa.oms.order.modules.order.service.IOrderResponseCreatorService;
import com.sqa.oms.order.service.order.IModifyOrderService;
import com.sqa.oms.order.util.Check;
import org.springframework.stereotype.Service;

/**
 * Default implementation of the update order and update order status response builder.
 */
@Service
public class ModifyOrderResponseCreatorService implements IModifyOrderResponseCreatorService {

    private final IModifyOrderService modifyOrderService;

    private final IOrderResponseCreatorService orderResponseCreatorService;

    /**
     * Constructor with dependency injection.
     *
     * @param modifyOrderService          core order update flow
     * @param orderResponseCreatorService builds the full response off the just updated entityId
     */
    public ModifyOrderResponseCreatorService(final IModifyOrderService modifyOrderService,
                                             final IOrderResponseCreatorService orderResponseCreatorService) {
        Check.isNotNull(modifyOrderService, "modifyOrderService");
        Check.isNotNull(orderResponseCreatorService, "orderResponseCreatorService");

        this.modifyOrderService = modifyOrderService;
        this.orderResponseCreatorService = orderResponseCreatorService;
    }

    @Override
    public OrderResponse update(final String entityId, final UpdateOrderRequest request) {
        Check.isNotEmpty(entityId, "entityId");
        Check.isNotNull(request, "request");

        modifyOrderService.update(entityId, request);

        return orderResponseCreatorService.create(entityId);
    }

    @Override
    public OrderResponse updateStatus(final String entityId, final UpdateOrderStatusRequest request) {
        Check.isNotEmpty(entityId, "entityId");
        Check.isNotNull(request, "request");

        modifyOrderService.updateStatus(entityId, request);

        return orderResponseCreatorService.create(entityId);
    }
}

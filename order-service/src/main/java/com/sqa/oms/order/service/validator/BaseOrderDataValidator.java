package com.sqa.oms.order.service.validator;

import com.sqa.oms.order.exception.BusinessException;
import com.sqa.oms.order.modules.order.model.OrderItemRequest;
import com.sqa.oms.order.util.StringHelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared validation rules of the order create and update flows.
 */
public abstract class BaseOrderDataValidator {

    /**
     * Validates the item list of a create or update request.
     *
     * @param items requested order items
     */
    protected void validateItems(final List<OrderItemRequest> items) {
        List<String> messages = new ArrayList<>();

        if (items == null || items.isEmpty()) {
            messages.add("An order must contain at least one item.");
        } else {
            for (OrderItemRequest item : items) {
                collectItemMessages(item, messages);
            }
        }

        if (!messages.isEmpty()) {
            throw new BusinessException(messages);
        }
    }

    private void collectItemMessages(final OrderItemRequest item, final List<String> messages) {
        if (item == null) {
            messages.add("An order item must not be null.");

            return;
        }

        boolean hasProduct = item.productId() != null;
        boolean hasName = !StringHelper.isEmpty(item.productName());

        if (!hasProduct && !hasName) {
            messages.add("An order item requires either a productId or a productName.");
        }

        if (item.quantity() == null || item.quantity() <= 0) {
            messages.add("Order item quantity must be a positive number.");
        }

        if (item.unitPrice() == null || item.unitPrice().compareTo(BigDecimal.ZERO) < 0) {
            messages.add("Order item unit price must not be negative.");
        }
    }
}

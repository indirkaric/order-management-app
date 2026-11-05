package com.sqa.oms.ui.modules.order.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request body of the update order endpoint.
 *
 * @param items replacement set of ordered items
 */
public record UpdateOrderRequest(@NotEmpty List<@Valid OrderItemRequest> items) {
}

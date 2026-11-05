package com.sqa.oms.ui.modules.order.model;

import jakarta.validation.constraints.NotNull;

/**
 * Request body of the update order status endpoint.
 *
 * @param status the requested target status
 */
public record UpdateOrderStatusRequest(@NotNull OrderStatus status) {
}

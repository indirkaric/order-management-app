package com.sqa.oms.ui.modules.order.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request body of the create order endpoint.
 *
 * @param customerId customer to order for; ignored for a non elevated caller
 * @param items      ordered items
 */
public record CreateOrderRequest(String customerId,
                                 @NotEmpty List<@Valid OrderItemRequest> items) {
}

package com.sqa.oms.order.modules.order.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UpdateOrderRequest(@NotEmpty List<@Valid OrderItemRequest> items) {
}

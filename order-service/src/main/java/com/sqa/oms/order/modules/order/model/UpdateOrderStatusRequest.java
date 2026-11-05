package com.sqa.oms.order.modules.order.model;

import com.sqa.oms.order.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(@NotNull OrderStatus status) {
}

package com.sqa.oms.order.modules.order.model;

import com.sqa.oms.order.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(String entityId,
                            String orderNumber,
                            String customerId,
                            OrderStatus status,
                            BigDecimal totalAmount,
                            List<OrderItemResponse> items,
                            Instant createdUtc) {
}

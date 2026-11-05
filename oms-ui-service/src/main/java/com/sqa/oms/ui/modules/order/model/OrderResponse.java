package com.sqa.oms.ui.modules.order.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * DTO of the Order entity, mirroring order-service's own response shape.
 *
 * @param id          order id
 * @param orderNumber human readable order number
 * @param customerId  owning customer
 * @param status      current lifecycle status
 * @param totalAmount sum of every item's line total
 * @param items       ordered items
 * @param createdUtc  creation timestamp
 */
public record OrderResponse(Long id,
                            String orderNumber,
                            String customerId,
                            OrderStatus status,
                            BigDecimal totalAmount,
                            List<OrderItemResponse> items,
                            Instant createdUtc) {
}

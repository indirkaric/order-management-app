package com.sqa.oms.order.modules.order.repository.projection;

import com.sqa.oms.order.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Column level projection of the orders table, selected directly by the Criteria API queries
 * instead of loading the full {@code Order} entity. The items of the order are deliberately left
 * out, the same way a user projection leaves out its groups, and are fetched separately.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderProjection {

    private String entityId;

    private String orderNumber;

    private String customerId;

    private OrderStatus status;

    private BigDecimal totalAmount;

    private Instant createdUtc;
}

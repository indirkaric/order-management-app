package com.sqa.oms.order.modules.order.repository.projection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Short, column level projection of a single order item, used to build the {@code items} of the
 * order response without loading full {@code OrderItem} entities.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemProjection {

    private String productId;

    private String productName;

    private Integer quantity;

    private BigDecimal unitPrice;
}

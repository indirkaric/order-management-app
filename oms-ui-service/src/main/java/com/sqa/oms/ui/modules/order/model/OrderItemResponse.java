package com.sqa.oms.ui.modules.order.model;

import java.math.BigDecimal;

/**
 * DTO of a single order item, mirroring order-service's own response shape.
 *
 * @param id          line item id
 * @param productId   referenced catalog product id, or null for a free-text line
 * @param productName display name
 * @param quantity    ordered quantity
 * @param unitPrice   price per unit
 * @param lineTotal   quantity multiplied by unitPrice
 */
public record OrderItemResponse(Long id,
                                Long productId,
                                String productName,
                                Integer quantity,
                                BigDecimal unitPrice,
                                BigDecimal lineTotal) {
}

package com.sqa.oms.ui.modules.order.model;

import java.math.BigDecimal;

/**
 * DTO of the Product entity, mirroring order-service's own response shape.
 *
 * @param id          product id
 * @param entityId    business key
 * @param name        product name
 * @param sku         stock keeping unit
 * @param description free text description
 * @param unitPrice   catalog price
 */
public record ProductResponse(Long id,
                              String entityId,
                              String name,
                              String sku,
                              String description,
                              BigDecimal unitPrice) {
}

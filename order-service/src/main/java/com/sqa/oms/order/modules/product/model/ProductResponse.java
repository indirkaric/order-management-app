package com.sqa.oms.order.modules.product.model;

import java.math.BigDecimal;

public record ProductResponse(String entityId,
                              String name,
                              String sku,
                              String description,
                              BigDecimal unitPrice) {
}

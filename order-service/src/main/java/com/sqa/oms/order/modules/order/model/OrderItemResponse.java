package com.sqa.oms.order.modules.order.model;

import java.math.BigDecimal;

public record OrderItemResponse(String productId,
                                String productName,
                                Integer quantity,
                                BigDecimal unitPrice,
                                BigDecimal lineTotal) {
}

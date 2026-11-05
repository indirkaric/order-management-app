package com.sqa.oms.order.modules.order.dto;

import com.sqa.oms.order.enums.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Service layer representation of an order, returned by {@code IOrderService} to the response
 * creator services. The items of the order are not part of this DTO; they are fetched and
 * attached separately while the response is being built.
 */
@Data
public class OrderDTO {

    private String entityId;

    private String orderNumber;

    private String customerId;

    private OrderStatus status;

    private BigDecimal totalAmount;

    private Instant createdUtc;
}

package com.sqa.oms.ui.modules.order.model;

/**
 * Lifecycle states of an order, mirroring order-service's own enum.
 */
public enum OrderStatus {

    CREATED,
    CONFIRMED,
    SHIPPED,
    CANCELLED
}

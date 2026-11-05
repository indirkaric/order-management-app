package com.sqa.oms.order.modules.order.mapper;

import com.sqa.oms.order.entity.OrderItem;

import java.math.BigDecimal;
import java.util.List;

/**
 * Single place where order money is computed, so that the stored total and the exposed line
 * totals can never disagree.
 */
public final class OrderAmountCalculator {

    private OrderAmountCalculator() {
    }

    /**
     * Computes quantity multiplied by unit price for one item.
     *
     * @param item order item
     * @return the line total
     */
    public static BigDecimal lineTotal(final OrderItem item) {
        if (item == null) {
            return BigDecimal.ZERO;
        }

        return lineTotal(item.getUnitPrice(), item.getQuantity());
    }

    /**
     * Computes quantity multiplied by unit price, for callers that only have the two raw values
     * at hand (e.g. an {@code OrderItemProjection} instead of a loaded entity).
     *
     * @param unitPrice unit price
     * @param quantity  quantity
     * @return the line total
     */
    public static BigDecimal lineTotal(final BigDecimal unitPrice, final Integer quantity) {
        if (unitPrice == null || quantity == null) {
            return BigDecimal.ZERO;
        }

        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Computes the sum of all line totals of an order.
     *
     * @param items order items
     * @return the order total
     */
    public static BigDecimal total(final List<OrderItem> items) {
        if (items == null) {
            return BigDecimal.ZERO;
        }

        return items.stream()
                .map(OrderAmountCalculator::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

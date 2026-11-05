package com.sqa.oms.order.enums;

import java.util.Set;

/**
 * Lifecycle states of an order together with the legal transitions between them.
 */
public enum OrderStatus {

    CREATED,
    CONFIRMED,
    SHIPPED,
    CANCELLED;

    private static final Set<OrderStatus> FROM_CREATED = Set.of(CONFIRMED, CANCELLED);

    private static final Set<OrderStatus> FROM_CONFIRMED = Set.of(SHIPPED, CANCELLED);

    /**
     * Tells whether moving this status to the given target status is allowed.
     *
     * @param target status the order should move to
     * @return true when the transition is legal
     */
    public boolean canTransitionTo(final OrderStatus target) {
        if (target == null) {
            return false;
        }

        return switch (this) {
            case CREATED -> FROM_CREATED.contains(target);
            case CONFIRMED -> FROM_CONFIRMED.contains(target);
            default -> false;
        };
    }
}

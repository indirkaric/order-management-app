package com.sqa.oms.order.event;

import com.sqa.oms.common.events.EntityType;
import com.sqa.oms.common.events.EventAction;

/**
 * Local Spring application event raised from inside a transaction. It is turned into an
 * {@link com.sqa.oms.common.events.OmsEvent} only once the transaction has committed.
 *
 * @param entityType  type of the entity that changed
 * @param action      what happened to the entity
 * @param entityId    business key of the entity
 * @param performedBy identity of the caller that triggered the change
 * @param payload     state to publish as the event payload
 */
public record DomainChangeEvent(EntityType entityType,
                                EventAction action,
                                String entityId,
                                String performedBy,
                                Object payload) {
}

package com.sqa.oms.identity.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.sqa.oms.common.events.EntityType;
import com.sqa.oms.common.events.EventAction;

/**
 * Local Spring application event raised inside a transactional service method. It is turned into an
 * {@code OmsEvent} and pushed to Kafka only after the surrounding transaction has committed.
 *
 * @param entityType type of the changed entity
 * @param action     kind of change
 * @param entityId   business key of the changed entity
 * @param performedBy identity of the caller that made the change
 * @param payload    snapshot of the changed entity
 */
public record DomainChangeEvent(EntityType entityType,
                                EventAction action,
                                String entityId,
                                String performedBy,
                                JsonNode payload) {
}

package com.sqa.oms.ui.modules.audit.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;

/**
 * Audit log entry exposed by the gateway, mirroring audit-log-service's own response shape.
 * The payload stays a raw {@link JsonNode} since it is a genuine entity snapshot whose shape
 * depends on the entity type that changed.
 *
 * @param id            store specific identifier
 * @param eventId       identifier of the originating Kafka event
 * @param eventType     event type, for example ORDER_CREATED
 * @param action        CREATE, UPDATE or DELETE
 * @param entityType    type of the changed entity
 * @param entityId      identifier of the changed entity
 * @param sourceService service that emitted the event
 * @param performedBy   user that performed the change
 * @param timestamp     moment the change happened
 * @param payload       entity snapshot carried by the event
 */
public record AuditLogResponse(
        String id,
        String eventId,
        String eventType,
        String action,
        String entityType,
        String entityId,
        String sourceService,
        String performedBy,
        Instant timestamp,
        JsonNode payload) {
}

package com.sqa.oms.auditlog.modules.audit_log.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;

/**
 * Audit log entry exposed by the query API.
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

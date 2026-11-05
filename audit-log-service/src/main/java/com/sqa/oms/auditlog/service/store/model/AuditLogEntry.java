package com.sqa.oms.auditlog.service.store.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Persistence agnostic representation of a single stored audit event.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogEntry {

    private String id;

    private String eventId;

    private String eventType;

    private String action;

    private String entityType;

    private String entityId;

    private String sourceService;

    private String performedBy;

    private Instant timestamp;

    private JsonNode payload;
}

package com.sqa.oms.auditlog.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Internal representation of a single audit log entry, built off {@code AuditLogEntry}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDTO {

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

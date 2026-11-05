package com.sqa.oms.auditlog.modules.audit_log.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;

/**
 * Query parameters accepted by the audit log search endpoint.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogSearchRequest {

    private String entityType;

    private String entityId;

    private String action;

    private String performedBy;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant from;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant to;
}

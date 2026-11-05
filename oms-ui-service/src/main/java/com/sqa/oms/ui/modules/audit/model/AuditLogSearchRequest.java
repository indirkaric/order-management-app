package com.sqa.oms.ui.modules.audit.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;

/**
 * Filters of the audit log search endpoint, mirroring audit-log-service's own request shape.
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

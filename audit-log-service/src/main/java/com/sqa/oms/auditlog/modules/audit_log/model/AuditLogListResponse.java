package com.sqa.oms.auditlog.modules.audit_log.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Paged response of the audit log search endpoint.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditLogListResponse {

    private List<AuditLogResponse> data = new ArrayList<>();

    private long count;
}

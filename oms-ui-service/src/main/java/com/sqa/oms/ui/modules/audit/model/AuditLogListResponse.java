package com.sqa.oms.ui.modules.audit.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Paged response of the audit log search endpoint, mirroring audit-log-service's own
 * {@code AuditLogListResponse} shape ({@code data}/{@code count}, not a Spring Data {@code Page}).
 */
@Data
public class AuditLogListResponse {

    private List<AuditLogResponse> data = new ArrayList<>();

    private long count;
}

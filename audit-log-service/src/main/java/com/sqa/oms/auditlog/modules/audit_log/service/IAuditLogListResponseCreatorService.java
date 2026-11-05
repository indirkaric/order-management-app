package com.sqa.oms.auditlog.modules.audit_log.service;

import com.sqa.oms.auditlog.modules.audit_log.model.AuditLogListResponse;
import com.sqa.oms.auditlog.modules.audit_log.model.AuditLogSearchRequest;
import org.springframework.data.domain.Pageable;

/**
 * Builds the response of the audit log search endpoint.
 */
public interface IAuditLogListResponseCreatorService {

    /**
     * Returns the filtered and paged list of audit log entries.
     *
     * @param request  the query filters
     * @param pageable paging and sorting
     * @return AuditLogListResponse
     */
    AuditLogListResponse create(AuditLogSearchRequest request, Pageable pageable);
}

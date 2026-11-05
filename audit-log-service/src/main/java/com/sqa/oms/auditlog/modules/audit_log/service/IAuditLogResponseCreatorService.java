package com.sqa.oms.auditlog.modules.audit_log.service;

import com.sqa.oms.auditlog.modules.audit_log.model.AuditLogResponse;

/**
 * Builds the response of the single audit log entry endpoint.
 */
public interface IAuditLogResponseCreatorService {

    /**
     * Returns a single audit log entry.
     *
     * @param id the store specific identifier
     * @return AuditLogResponse
     */
    AuditLogResponse create(String id);
}

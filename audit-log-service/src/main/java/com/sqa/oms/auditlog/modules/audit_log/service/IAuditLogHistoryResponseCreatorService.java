package com.sqa.oms.auditlog.modules.audit_log.service;

import com.sqa.oms.auditlog.modules.audit_log.model.AuditLogResponse;

import java.util.List;

/**
 * Builds the response of the entity change history endpoint.
 */
public interface IAuditLogHistoryResponseCreatorService {

    /**
     * Returns the full change history of a single entity, oldest event first.
     *
     * @param entityType the entity type
     * @param entityId   the entity identifier
     * @return the ordered list of audit log responses
     */
    List<AuditLogResponse> create(String entityType, String entityId);
}

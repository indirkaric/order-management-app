package com.sqa.oms.ui.service.audit;

import com.sqa.oms.ui.modules.audit.model.AuditLogListResponse;
import com.sqa.oms.ui.modules.audit.model.AuditLogResponse;
import com.sqa.oms.ui.modules.audit.model.AuditLogSearchRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Proxy to the audit log service.
 */
public interface IAuditLogService {

    /**
     * Searches the audit log.
     *
     * @param request  the query parameters
     * @param pageable paging information
     * @return audit log list
     */
    AuditLogListResponse getAll(AuditLogSearchRequest request, Pageable pageable);

    /**
     * Retrieves a single audit log entry.
     *
     * @param auditLogId audit log identifier
     * @return audit log entry
     */
    AuditLogResponse getById(String auditLogId);

    /**
     * Retrieves the full change history of a single entity.
     *
     * @param entityType the entity type
     * @param entityId   the entity identifier
     * @return the ordered change history
     */
    List<AuditLogResponse> getEntityHistory(String entityType, String entityId);
}

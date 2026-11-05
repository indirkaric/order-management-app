package com.sqa.oms.auditlog.service.audit_log;

import com.sqa.oms.auditlog.dto.AuditLogDTO;
import com.sqa.oms.auditlog.modules.audit_log.model.AuditLogSearchRequest;
import com.sqa.oms.auditlog.service.model.PagedList;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Core read access to the audit log, independent of the active store implementation.
 */
public interface IAuditLogService {

    /**
     * Returns a single audit log entry.
     *
     * @param id the store specific identifier
     * @return the audit log entry
     */
    AuditLogDTO findById(String id);

    /**
     * Returns the filtered and paged list of audit log entries.
     *
     * @param request  the query filters
     * @param pageable paging and sorting
     * @return the matching paged list of audit log entries
     */
    PagedList<AuditLogDTO> findPagedList(AuditLogSearchRequest request, Pageable pageable);

    /**
     * Returns the full change history of a single entity, oldest event first.
     *
     * @param entityType the entity type
     * @param entityId   the entity identifier
     * @return the ordered list of audit log entries
     */
    List<AuditLogDTO> findEntityHistory(String entityType, String entityId);
}

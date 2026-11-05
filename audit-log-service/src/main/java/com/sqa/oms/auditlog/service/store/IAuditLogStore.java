package com.sqa.oms.auditlog.service.store;

import com.sqa.oms.auditlog.modules.audit_log.model.AuditLogSearchRequest;
import com.sqa.oms.auditlog.service.store.model.AuditLogEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Abstraction over the audit log persistence technology. Implementations are fully substitutable: callers only
 * ever see AuditLogEntry and AuditLogSearchRequest, never a JPA entity or an Elasticsearch document. The identifier
 * of an entry is store specific and must only be fed back into the same store.
 */
public interface IAuditLogStore {

    /**
     * Persists a new audit log entry.
     *
     * @param entry the entry to persist
     */
    void save(AuditLogEntry entry);

    /**
     * Tells whether an event has already been stored.
     *
     * @param eventId the event identifier carried by the Kafka message
     * @return true when an entry with the given event id is already stored
     */
    boolean existsByEventId(String eventId);

    /**
     * Returns a page of entries matching the given filter.
     *
     * @param request  the query filters, may be null to match everything
     * @param pageable the paging information
     * @return a page of matching entries
     */
    Page<AuditLogEntry> findPaged(AuditLogSearchRequest request, Pageable pageable);

    /**
     * Returns the full change history of a single entity, oldest event first.
     *
     * @param entityType the entity type
     * @param entityId   the entity identifier
     * @return the ordered list of entries
     */
    List<AuditLogEntry> findByEntity(String entityType, String entityId);

    /**
     * Returns a single entry by its store specific identifier.
     *
     * @param id the store specific identifier
     * @return the entry when present
     */
    Optional<AuditLogEntry> findById(String id);
}

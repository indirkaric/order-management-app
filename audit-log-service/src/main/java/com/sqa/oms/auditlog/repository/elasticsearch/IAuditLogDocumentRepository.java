package com.sqa.oms.auditlog.repository.elasticsearch;

import com.sqa.oms.auditlog.document.AuditLogDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * Spring Data Elasticsearch repository for the default audit log store.
 */
public interface IAuditLogDocumentRepository extends ElasticsearchRepository<AuditLogDocument, String> {

    /**
     * Tells whether a document with the given event id exists.
     *
     * @param eventId the event identifier
     * @return true when the event is already indexed
     */
    boolean existsByEventId(String eventId);

    /**
     * Returns the change history of a single entity, oldest event first.
     *
     * @param entityType the entity type
     * @param entityId   the entity identifier
     * @return the ordered list of documents
     */
    List<AuditLogDocument> findByEntityTypeAndEntityIdOrderByTimestampAsc(String entityType, String entityId);
}

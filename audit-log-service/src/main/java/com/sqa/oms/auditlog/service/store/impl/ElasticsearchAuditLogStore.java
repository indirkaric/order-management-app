package com.sqa.oms.auditlog.service.store.impl;

import com.sqa.oms.auditlog.document.AuditLogDocument;
import com.sqa.oms.auditlog.modules.audit_log.model.AuditLogSearchRequest;
import com.sqa.oms.auditlog.repository.elasticsearch.IAuditLogDocumentRepository;
import com.sqa.oms.auditlog.service.store.IAuditLogStore;
import com.sqa.oms.auditlog.service.store.model.AuditLogEntry;
import com.sqa.oms.auditlog.util.Check;
import com.sqa.oms.auditlog.util.JsonPayloads;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Elasticsearch audit log store - the sole persistence technology for this service.
 */
@Component
public class ElasticsearchAuditLogStore implements IAuditLogStore {

    private static final String FIELD_EVENT_ID = "eventId";

    private static final String FIELD_ENTITY_TYPE = "entityType";

    private static final String FIELD_ENTITY_ID = "entityId";

    private static final String FIELD_ACTION = "action";

    private static final String FIELD_PERFORMED_BY = "performedBy";

    private static final String FIELD_TIMESTAMP = "timestamp";

    private final IAuditLogDocumentRepository auditLogDocumentRepository;

    private final ElasticsearchOperations elasticsearchOperations;

    /**
     * Constructor with dependency injection.
     *
     * @param auditLogDocumentRepository the audit log document repository
     * @param elasticsearchOperations    operations used for the filtered paged search
     */
    public ElasticsearchAuditLogStore(final IAuditLogDocumentRepository auditLogDocumentRepository,
                                      final ElasticsearchOperations elasticsearchOperations) {
        Check.isNotNull(auditLogDocumentRepository, "auditLogDocumentRepository");
        Check.isNotNull(elasticsearchOperations, "elasticsearchOperations");
        this.auditLogDocumentRepository = auditLogDocumentRepository;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    /**
     * Persists a new audit log entry.
     *
     * @param entry the entry to persist
     */
    @Override
    public final void save(final AuditLogEntry entry) {
        Check.isNotNull(entry, "entry");

        auditLogDocumentRepository.save(toDocument(entry));
    }

    /**
     * Tells whether an event has already been stored.
     *
     * @param eventId the event identifier carried by the Kafka message
     * @return true when an entry with the given event id is already stored
     */
    @Override
    public final boolean existsByEventId(final String eventId) {
        Check.isNotEmpty(eventId, "eventId");

        return auditLogDocumentRepository.existsByEventId(eventId);
    }

    /**
     * Returns a page of entries matching the given filter.
     *
     * @param request  the query filters, may be null to match everything
     * @param pageable the paging information
     * @return a page of matching entries
     */
    @Override
    public final Page<AuditLogEntry> findPaged(final AuditLogSearchRequest request, final Pageable pageable) {
        Check.isNotNull(pageable, "pageable");

        CriteriaQuery query = new CriteriaQuery(buildCriteria(request), pageable);
        SearchHits<AuditLogDocument> searchHits = elasticsearchOperations.search(query, AuditLogDocument.class);

        List<AuditLogEntry> entries = new ArrayList<>();
        for (SearchHit<AuditLogDocument> searchHit : searchHits.getSearchHits()) {
            entries.add(toEntry(searchHit.getContent()));
        }

        return new PageImpl<>(entries, pageable, searchHits.getTotalHits());
    }

    /**
     * Returns the full change history of a single entity.
     *
     * @param entityType the entity type
     * @param entityId   the entity identifier
     * @return the ordered list of entries
     */
    @Override
    public final List<AuditLogEntry> findByEntity(final String entityType, final String entityId) {
        Check.isNotEmpty(entityType, "entityType");
        Check.isNotEmpty(entityId, "entityId");

        return auditLogDocumentRepository.findByEntityTypeAndEntityIdOrderByTimestampAsc(entityType, entityId)
                .stream()
                .map(ElasticsearchAuditLogStore::toEntry)
                .toList();
    }

    /**
     * Returns a single entry by its store specific identifier.
     *
     * @param id the store specific identifier
     * @return the entry when present
     */
    @Override
    public final Optional<AuditLogEntry> findById(final String id) {
        Check.isNotNull(id, "id");

        if (!StringUtils.hasText(id)) {
            return Optional.empty();
        }

        return auditLogDocumentRepository.findById(id).map(ElasticsearchAuditLogStore::toEntry);
    }

    private static Criteria buildCriteria(final AuditLogSearchRequest request) {
        List<Criteria> predicates = new ArrayList<>();
        if (request != null) {
            addEqualIfPresent(predicates, FIELD_ENTITY_TYPE, request.getEntityType());
            addEqualIfPresent(predicates, FIELD_ENTITY_ID, request.getEntityId());
            addEqualIfPresent(predicates, FIELD_ACTION, request.getAction());
            addEqualIfPresent(predicates, FIELD_PERFORMED_BY, request.getPerformedBy());

            if (request.getFrom() != null) {
                predicates.add(new Criteria(FIELD_TIMESTAMP).greaterThanEqual(request.getFrom()));
            }

            if (request.getTo() != null) {
                predicates.add(new Criteria(FIELD_TIMESTAMP).lessThanEqual(request.getTo()));
            }
        }

        if (predicates.isEmpty()) {
            // Matches every stored document, since an audit document always carries an event id.
            return new Criteria(FIELD_EVENT_ID).exists();
        }

        Criteria criteria = predicates.get(0);
        for (int index = 1; index < predicates.size(); index++) {
            criteria = criteria.and(predicates.get(index));
        }

        return criteria;
    }

    private static void addEqualIfPresent(final List<Criteria> predicates,
                                          final String field,
                                          final String value) {
        if (StringUtils.hasText(value)) {
            predicates.add(new Criteria(field).is(value));
        }
    }

    private static AuditLogDocument toDocument(final AuditLogEntry entry) {
        return AuditLogDocument.builder()
                // The event id doubles as the document id, so a replayed message overwrites instead of duplicating.
                .id(entry.getEventId())
                .eventId(entry.getEventId())
                .eventType(entry.getEventType())
                .action(entry.getAction())
                .entityType(entry.getEntityType())
                .entityId(entry.getEntityId())
                .sourceService(entry.getSourceService())
                .performedBy(entry.getPerformedBy())
                .timestamp(entry.getTimestamp())
                .payload(JsonPayloads.toJson(entry.getPayload()))
                .build();
    }

    private static AuditLogEntry toEntry(final AuditLogDocument document) {
        return AuditLogEntry.builder()
                .id(document.getId())
                .eventId(document.getEventId())
                .eventType(document.getEventType())
                .action(document.getAction())
                .entityType(document.getEntityType())
                .entityId(document.getEntityId())
                .sourceService(document.getSourceService())
                .performedBy(document.getPerformedBy())
                .timestamp(document.getTimestamp())
                .payload(JsonPayloads.toNode(document.getPayload()))
                .build();
    }
}

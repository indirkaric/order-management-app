package com.sqa.oms.auditlog.service.store.impl;

import com.sqa.oms.auditlog.document.AuditLogDocument;
import com.sqa.oms.auditlog.modules.audit_log.model.AuditLogSearchRequest;
import com.sqa.oms.auditlog.repository.elasticsearch.IAuditLogDocumentRepository;
import com.sqa.oms.auditlog.service.store.model.AuditLogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ElasticsearchAuditLogStoreTest {

    private static final int PAGE_SIZE = 10;

    @Mock
    private IAuditLogDocumentRepository auditLogDocumentRepository;

    @Mock
    private ElasticsearchOperations elasticsearchOperations;

    private ElasticsearchAuditLogStore elasticsearchAuditLogStore;

    @BeforeEach
    void setUp() {
        elasticsearchAuditLogStore = new ElasticsearchAuditLogStore(auditLogDocumentRepository,
                elasticsearchOperations);
    }

    @Test
    void shouldConvertTheEntryIntoADocumentKeyedByItsEventIdWhenSaving() {
        AuditLogEntry entry = AuditLogEntry.builder()
                .eventId("evt-1")
                .eventType("ORDER_CREATED")
                .action("CREATE")
                .entityType("ORDER")
                .entityId("ORD-1")
                .sourceService("order-service")
                .performedBy("caller")
                .timestamp(Instant.EPOCH)
                .build();

        elasticsearchAuditLogStore.save(entry);

        ArgumentCaptor<AuditLogDocument> captor = ArgumentCaptor.forClass(AuditLogDocument.class);
        verify(auditLogDocumentRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo("evt-1");
        assertThat(captor.getValue().getEventId()).isEqualTo("evt-1");
        assertThat(captor.getValue().getEntityId()).isEqualTo("ORD-1");
    }

    @Test
    void shouldDelegateToTheRepositoryWhenCheckingExistsByEventId() {
        when(auditLogDocumentRepository.existsByEventId("evt-1")).thenReturn(true);

        assertThat(elasticsearchAuditLogStore.existsByEventId("evt-1")).isTrue();
    }

    @Test
    void shouldMapEveryHitBackToAnEntryWhenFindingPaged() {
        AuditLogDocument document = AuditLogDocument.builder().id("evt-1").eventId("evt-1").build();
        SearchHit<AuditLogDocument> searchHit = mockSearchHit(document);

        SearchHits<AuditLogDocument> searchHits = mockSearchHits(List.of(searchHit), 1L);
        when(elasticsearchOperations.search(any(Query.class), eq(AuditLogDocument.class))).thenReturn(searchHits);

        Page<AuditLogEntry> page = elasticsearchAuditLogStore.findPaged(
                new AuditLogSearchRequest("ORDER", null, null, null, null, null), PageRequest.of(0, PAGE_SIZE));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getEventId()).isEqualTo("evt-1");
        assertThat(page.getTotalElements()).isEqualTo(1L);
    }

    @Test
    void shouldWorkWithoutAnyFilterWhenFindingPaged() {
        SearchHits<AuditLogDocument> searchHits = mockSearchHits(List.of(), 0L);
        when(elasticsearchOperations.search(any(Query.class), eq(AuditLogDocument.class))).thenReturn(searchHits);

        Page<AuditLogEntry> page = elasticsearchAuditLogStore.findPaged(null, PageRequest.of(0, PAGE_SIZE));

        assertThat(page.getContent()).isEmpty();
    }

    @Test
    void shouldMapEveryDocumentWhenFindingByEntity() {
        AuditLogDocument document = AuditLogDocument.builder().id("evt-1").eventId("evt-1").build();
        when(auditLogDocumentRepository.findByEntityTypeAndEntityIdOrderByTimestampAsc("ORDER", "ORD-1"))
                .thenReturn(List.of(document));

        List<AuditLogEntry> entries = elasticsearchAuditLogStore.findByEntity("ORDER", "ORD-1");

        assertThat(entries).hasSize(1);
        assertThat(entries.get(0).getEventId()).isEqualTo("evt-1");
    }

    @Test
    void shouldReturnEmptyForABlankIdWhenFindingById() {
        assertThat(elasticsearchAuditLogStore.findById("  ")).isEmpty();
    }

    @Test
    void shouldDelegateToTheRepositoryForARealIdWhenFindingById() {
        AuditLogDocument document = AuditLogDocument.builder().id("evt-1").eventId("evt-1").build();
        when(auditLogDocumentRepository.findById("evt-1")).thenReturn(Optional.of(document));

        Optional<AuditLogEntry> result = elasticsearchAuditLogStore.findById("evt-1");

        assertThat(result).isPresent();
        assertThat(result.get().getEventId()).isEqualTo("evt-1");
    }

    @SuppressWarnings("unchecked")
    private SearchHit<AuditLogDocument> mockSearchHit(final AuditLogDocument document) {
        SearchHit<AuditLogDocument> searchHit = org.mockito.Mockito.mock(SearchHit.class);
        when(searchHit.getContent()).thenReturn(document);

        return searchHit;
    }

    @SuppressWarnings("unchecked")
    private SearchHits<AuditLogDocument> mockSearchHits(final List<SearchHit<AuditLogDocument>> hits,
                                                         final long totalHits) {
        SearchHits<AuditLogDocument> searchHits = org.mockito.Mockito.mock(SearchHits.class);
        when(searchHits.getSearchHits()).thenReturn(hits);
        when(searchHits.getTotalHits()).thenReturn(totalHits);

        return searchHits;
    }
}

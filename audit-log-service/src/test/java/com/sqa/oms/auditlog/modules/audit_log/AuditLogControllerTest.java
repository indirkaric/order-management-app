package com.sqa.oms.auditlog.modules.audit_log;

import com.sqa.oms.auditlog.modules.audit_log.model.AuditLogListResponse;
import com.sqa.oms.auditlog.modules.audit_log.model.AuditLogResponse;
import com.sqa.oms.auditlog.modules.audit_log.model.AuditLogSearchRequest;
import com.sqa.oms.auditlog.modules.audit_log.service.IAuditLogHistoryResponseCreatorService;
import com.sqa.oms.auditlog.modules.audit_log.service.IAuditLogListResponseCreatorService;
import com.sqa.oms.auditlog.modules.audit_log.service.IAuditLogResponseCreatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogControllerTest {

    private static final int PAGE_SIZE = 20;

    @Mock
    private IAuditLogListResponseCreatorService auditLogListResponseCreatorService;

    @Mock
    private IAuditLogResponseCreatorService auditLogResponseCreatorService;

    @Mock
    private IAuditLogHistoryResponseCreatorService auditLogHistoryResponseCreatorService;

    private AuditLogController auditLogController;

    @BeforeEach
    void setUp() {
        auditLogController = new AuditLogController(auditLogListResponseCreatorService,
                auditLogResponseCreatorService, auditLogHistoryResponseCreatorService);
    }

    @Test
    void shouldDelegateToTheServiceWhenSearching() {
        AuditLogSearchRequest request = new AuditLogSearchRequest();
        AuditLogResponse response = mockResponse();
        AuditLogListResponse listResponse = new AuditLogListResponse(List.of(response), 1);

        when(auditLogListResponseCreatorService.create(request, PageRequest.of(0, PAGE_SIZE)))
                .thenReturn(listResponse);

        ResponseEntity<AuditLogListResponse> result =
                auditLogController.search(request, PageRequest.of(0, PAGE_SIZE));

        assertThat(result.getBody()).isEqualTo(listResponse);
    }

    @Test
    void shouldReturnNoContentWhenSearchFindsNothing() {
        AuditLogSearchRequest request = new AuditLogSearchRequest();
        AuditLogListResponse emptyResponse = new AuditLogListResponse(List.of(), 0);

        when(auditLogListResponseCreatorService.create(request, PageRequest.of(0, PAGE_SIZE)))
                .thenReturn(emptyResponse);

        ResponseEntity<AuditLogListResponse> result =
                auditLogController.search(request, PageRequest.of(0, PAGE_SIZE));

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void shouldDelegateToTheServiceWhenFindingById() {
        AuditLogResponse response = mockResponse();
        when(auditLogResponseCreatorService.create("1")).thenReturn(response);

        ResponseEntity<AuditLogResponse> result = auditLogController.findById("1");

        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void shouldDelegateToTheServiceWhenFindingEntityHistory() {
        AuditLogResponse response = mockResponse();
        when(auditLogHistoryResponseCreatorService.create("ORDER", "ORD-1")).thenReturn(List.of(response));

        ResponseEntity<List<AuditLogResponse>> result = auditLogController.findEntityHistory("ORDER", "ORD-1");

        assertThat(result.getBody()).containsExactly(response);
    }

    @Test
    void shouldReturnNoContentWhenFindingEntityHistoryFindsNothing() {
        when(auditLogHistoryResponseCreatorService.create("ORDER", "ORD-1")).thenReturn(List.of());

        ResponseEntity<List<AuditLogResponse>> result = auditLogController.findEntityHistory("ORDER", "ORD-1");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    private AuditLogResponse mockResponse() {
        return new AuditLogResponse(
                "1", "evt-1", "ORDER_CREATED", "CREATE", "ORDER", "ORD-1", "order-service", "caller", null, null);
    }
}

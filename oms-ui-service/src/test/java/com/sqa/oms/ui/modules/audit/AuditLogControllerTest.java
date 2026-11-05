package com.sqa.oms.ui.modules.audit;

import com.sqa.oms.ui.modules.audit.model.AuditLogListResponse;
import com.sqa.oms.ui.modules.audit.model.AuditLogResponse;
import com.sqa.oms.ui.modules.audit.model.AuditLogSearchRequest;
import com.sqa.oms.ui.service.audit.IAuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogControllerTest {

    private static final int PAGE_SIZE = 50;

    @Mock
    private IAuditLogService auditLogService;

    private AuditLogController auditLogController;

    @BeforeEach
    void setUp() {
        auditLogController = new AuditLogController(auditLogService);
    }

    @Test
    void shouldDelegateToTheServiceWhenGettingAll() {
        AuditLogSearchRequest request = new AuditLogSearchRequest(null, null, null, null, null, null);
        AuditLogResponse entry = new AuditLogResponse("1", "evt-1", "ORDER_CREATED", "CREATE",
                "ORDER", "ORD-1", "order-service", "USR1", null, null);
        AuditLogListResponse response = new AuditLogListResponse();
        response.setData(List.of(entry));
        response.setCount(1);
        when(auditLogService.getAll(request, PageRequest.of(0, PAGE_SIZE))).thenReturn(response);

        assertThat(auditLogController.getAll(request, PageRequest.of(0, PAGE_SIZE)).getBody()).isEqualTo(response);
    }

    @Test
    void shouldReturnNoContentWhenNoAuditLogEntriesAreFound() {
        AuditLogSearchRequest request = new AuditLogSearchRequest(null, null, null, null, null, null);
        AuditLogListResponse response = new AuditLogListResponse();
        when(auditLogService.getAll(request, PageRequest.of(0, PAGE_SIZE))).thenReturn(response);

        assertThat(auditLogController.getAll(request, PageRequest.of(0, PAGE_SIZE)).getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void shouldDelegateToTheServiceWhenGettingEntityHistory() {
        List<AuditLogResponse> history = List.of(new AuditLogResponse("1", "evt-1", "ORDER_CREATED", "CREATE",
                "ORDER", "ORD-1", "order-service", "USR1", null, null));
        when(auditLogService.getEntityHistory("ORDER", "ORD-1")).thenReturn(history);

        assertThat(auditLogController.getEntityHistory("ORDER", "ORD-1").getBody()).isEqualTo(history);
    }

    @Test
    void shouldReturnNoContentWhenNoEntityHistoryIsFound() {
        when(auditLogService.getEntityHistory("ORDER", "ORD-1")).thenReturn(List.of());

        assertThat(auditLogController.getEntityHistory("ORDER", "ORD-1").getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
    }
}

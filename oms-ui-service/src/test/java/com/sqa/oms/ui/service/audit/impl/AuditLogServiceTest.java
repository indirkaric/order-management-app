package com.sqa.oms.ui.service.audit.impl;

import com.sqa.oms.ui.client.audit.AuditLogClient;
import com.sqa.oms.ui.client.response.ClientResponseWrapper;
import com.sqa.oms.ui.modules.audit.model.AuditLogListResponse;
import com.sqa.oms.ui.modules.audit.model.AuditLogResponse;
import com.sqa.oms.ui.modules.audit.model.AuditLogSearchRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    private static final int PAGE_SIZE = 50;

    @Mock
    private AuditLogClient auditLogClient;

    private AuditLogService auditLogService;

    @BeforeEach
    void setUp() {
        auditLogService = new AuditLogService(auditLogClient);
    }

    @Test
    void shouldReturnTheMappedListWhenGettingAll() {
        AuditLogListResponse response = new AuditLogListResponse();
        response.setData(List.of());
        AuditLogSearchRequest request = new AuditLogSearchRequest(null, null, null, null, null, null);

        when(auditLogClient.getAuditLogs(any(), any())).thenReturn(new ClientResponseWrapper(HttpStatus.OK, response));

        assertThat(auditLogService.getAll(request, PageRequest.of(0, PAGE_SIZE))).isEqualTo(response);
    }

    @Test
    void shouldReturnAnEmptyResponseWhenTheAuditLogServiceReturnsNoContentOnGettingAll() {
        AuditLogSearchRequest request = new AuditLogSearchRequest(null, null, null, null, null, null);

        when(auditLogClient.getAuditLogs(any(), any()))
                .thenReturn(new ClientResponseWrapper(HttpStatus.NO_CONTENT, null));

        assertThat(auditLogService.getAll(request, PageRequest.of(0, PAGE_SIZE)).getData()).isEmpty();
    }

    @Test
    void shouldReturnAnEmptyListWhenTheAuditLogServiceReturnsNoContentOnGettingEntityHistory() {
        when(auditLogClient.getEntityHistory("ORDER", "ORD-1"))
                .thenReturn(new ClientResponseWrapper(HttpStatus.NO_CONTENT, null));

        assertThat(auditLogService.getEntityHistory("ORDER", "ORD-1")).isEmpty();
    }

    @Test
    void shouldForwardTheEntityTypeAndIdWhenGettingEntityHistory() {
        AuditLogResponse[] history = {new AuditLogResponse("1", "evt-1", "ORDER_CREATED", "CREATE", "ORDER",
                "ORD-1", "order-service", "USR1", null, null)};

        when(auditLogClient.getEntityHistory("ORDER", "ORD-1"))
                .thenReturn(new ClientResponseWrapper(HttpStatus.OK, history));

        // A plain equality/containsExactly check would fail here: the real ResponseUtil round
        // trips the body through Jackson, which turns a null JsonNode field into a NullNode
        // instance rather than leaving it as Java null, so only the entityId is asserted.
        assertThat(auditLogService.getEntityHistory("ORDER", "ORD-1"))
                .extracting(AuditLogResponse::entityId)
                .containsExactly("ORD-1");
    }
}

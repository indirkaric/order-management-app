package com.sqa.oms.auditlog.modules.audit_log.service.impl;

import com.sqa.oms.auditlog.dto.AuditLogDTO;
import com.sqa.oms.auditlog.mapper.IAuditLogMapper;
import com.sqa.oms.auditlog.modules.audit_log.model.AuditLogResponse;
import com.sqa.oms.auditlog.service.audit_log.IAuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogHistoryResponseCreatorServiceTest {

    @Mock
    private IAuditLogService auditLogService;

    @Mock
    private IAuditLogMapper auditLogMapper;

    private AuditLogHistoryResponseCreatorService auditLogHistoryResponseCreatorService;

    @BeforeEach
    void setUp() {
        auditLogHistoryResponseCreatorService =
                new AuditLogHistoryResponseCreatorService(auditLogService, auditLogMapper);
    }

    @Test
    void shouldMapEveryEntryInOrderWhenCreatingTheHistoryResponse() {
        AuditLogDTO firstDto = AuditLogDTO.builder().id("1").build();
        AuditLogDTO secondDto = AuditLogDTO.builder().id("2").build();
        AuditLogResponse firstResponse = new AuditLogResponse(
                "1", "evt-1", "ORDER_CREATED", "CREATE", "ORDER", "ORD-1", "order-service", "caller", null, null);
        AuditLogResponse secondResponse = new AuditLogResponse(
                "2", "evt-2", "ORDER_UPDATED", "UPDATE", "ORDER", "ORD-1", "order-service", "caller", null, null);

        when(auditLogService.findEntityHistory("ORDER", "ORD-1")).thenReturn(List.of(firstDto, secondDto));
        when(auditLogMapper.toResponse(firstDto)).thenReturn(firstResponse);
        when(auditLogMapper.toResponse(secondDto)).thenReturn(secondResponse);

        List<AuditLogResponse> result = auditLogHistoryResponseCreatorService.create("ORDER", "ORD-1");

        assertThat(result).containsExactly(firstResponse, secondResponse);
    }
}

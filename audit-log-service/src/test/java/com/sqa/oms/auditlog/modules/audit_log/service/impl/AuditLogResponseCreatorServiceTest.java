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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogResponseCreatorServiceTest {

    @Mock
    private IAuditLogService auditLogService;

    @Mock
    private IAuditLogMapper auditLogMapper;

    private AuditLogResponseCreatorService auditLogResponseCreatorService;

    @BeforeEach
    void setUp() {
        auditLogResponseCreatorService = new AuditLogResponseCreatorService(auditLogService, auditLogMapper);
    }

    @Test
    void shouldDelegateToTheServiceAndMapperWhenCreatingTheResponse() {
        AuditLogDTO dto = AuditLogDTO.builder().id("1").eventId("evt-1").build();
        AuditLogResponse response = new AuditLogResponse(
                "1", "evt-1", "ORDER_CREATED", "CREATE", "ORDER", "ORD-1", "order-service", "caller", null, null);

        when(auditLogService.findById("1")).thenReturn(dto);
        when(auditLogMapper.toResponse(dto)).thenReturn(response);

        assertThat(auditLogResponseCreatorService.create("1")).isEqualTo(response);
    }
}

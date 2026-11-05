package com.sqa.oms.auditlog.modules.audit_log.service.impl;

import com.sqa.oms.auditlog.dto.AuditLogDTO;
import com.sqa.oms.auditlog.mapper.IAuditLogMapper;
import com.sqa.oms.auditlog.modules.audit_log.model.AuditLogListResponse;
import com.sqa.oms.auditlog.modules.audit_log.model.AuditLogResponse;
import com.sqa.oms.auditlog.modules.audit_log.model.AuditLogSearchRequest;
import com.sqa.oms.auditlog.service.audit_log.IAuditLogService;
import com.sqa.oms.auditlog.service.model.PagedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogListResponseCreatorServiceTest {

    private static final int PAGE_SIZE = 20;

    @Mock
    private IAuditLogService auditLogService;

    @Mock
    private IAuditLogMapper auditLogMapper;

    private AuditLogListResponseCreatorService auditLogListResponseCreatorService;

    @BeforeEach
    void setUp() {
        auditLogListResponseCreatorService = new AuditLogListResponseCreatorService(auditLogService, auditLogMapper);
    }

    @Test
    void shouldMapEveryItemAndWrapTheCountWhenCreatingTheListResponse() {
        AuditLogSearchRequest request = new AuditLogSearchRequest();
        AuditLogDTO dto = AuditLogDTO.builder().id("1").eventId("evt-1").build();
        AuditLogResponse response = new AuditLogResponse(
                "1", "evt-1", "ORDER_CREATED", "CREATE", "ORDER", "ORD-1", "order-service", "caller", null, null);

        when(auditLogService.findPagedList(request, PageRequest.of(0, PAGE_SIZE)))
                .thenReturn(new PagedList<>(List.of(dto), 1));
        when(auditLogMapper.toResponse(dto)).thenReturn(response);

        AuditLogListResponse result =
                auditLogListResponseCreatorService.create(request, PageRequest.of(0, PAGE_SIZE));

        assertThat(result.getData()).containsExactly(response);
        assertThat(result.getCount()).isEqualTo(1);
    }
}

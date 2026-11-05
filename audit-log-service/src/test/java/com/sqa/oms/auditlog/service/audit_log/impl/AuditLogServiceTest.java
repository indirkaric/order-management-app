package com.sqa.oms.auditlog.service.audit_log.impl;

import com.sqa.oms.auditlog.dto.AuditLogDTO;
import com.sqa.oms.auditlog.exception.EntityNotFoundException;
import com.sqa.oms.auditlog.mapper.IAuditLogMapper;
import com.sqa.oms.auditlog.modules.audit_log.model.AuditLogSearchRequest;
import com.sqa.oms.auditlog.service.model.PagedList;
import com.sqa.oms.auditlog.service.store.IAuditLogStore;
import com.sqa.oms.auditlog.service.store.model.AuditLogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    private static final int PAGE_SIZE = 10;

    @Mock
    private IAuditLogStore auditLogStore;

    @Mock
    private IAuditLogMapper auditLogMapper;

    private AuditLogService auditLogService;

    @BeforeEach
    void setUp() {
        auditLogService = new AuditLogService(auditLogStore, auditLogMapper);
    }

    @Test
    void shouldMapEveryStoredEntryWhenFindingPagedList() {
        AuditLogSearchRequest request = new AuditLogSearchRequest();
        AuditLogEntry entry = AuditLogEntry.builder().id("1").eventId("evt-1").build();
        AuditLogDTO dto = AuditLogDTO.builder().id("1").eventId("evt-1").build();

        when(auditLogStore.findPaged(request, PageRequest.of(0, PAGE_SIZE))).thenReturn(new PageImpl<>(List.of(entry)));
        when(auditLogMapper.toDto(entry)).thenReturn(dto);

        PagedList<AuditLogDTO> result = auditLogService.findPagedList(request, PageRequest.of(0, PAGE_SIZE));

        assertThat(result.getItems()).containsExactly(dto);
        assertThat(result.getCount()).isEqualTo(1);
    }

    @Test
    void shouldThrowWhenEntryDoesNotExist() {
        when(auditLogStore.findById("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditLogService.findById("unknown"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldReturnTheMappedEntryWhenFindingById() {
        AuditLogEntry entry = AuditLogEntry.builder().id("1").build();
        AuditLogDTO dto = AuditLogDTO.builder().id("1").build();

        when(auditLogStore.findById("1")).thenReturn(Optional.of(entry));
        when(auditLogMapper.toDto(entry)).thenReturn(dto);

        assertThat(auditLogService.findById("1")).isEqualTo(dto);
    }

    @Test
    void shouldMapEveryEntryInOrderWhenFindingEntityHistory() {
        AuditLogEntry first = AuditLogEntry.builder().id("1").build();
        AuditLogEntry second = AuditLogEntry.builder().id("2").build();
        AuditLogDTO firstDto = AuditLogDTO.builder().id("1").build();
        AuditLogDTO secondDto = AuditLogDTO.builder().id("2").build();

        when(auditLogStore.findByEntity("ORDER", "ORD-1")).thenReturn(List.of(first, second));
        when(auditLogMapper.toDto(first)).thenReturn(firstDto);
        when(auditLogMapper.toDto(second)).thenReturn(secondDto);

        List<AuditLogDTO> result = auditLogService.findEntityHistory("ORDER", "ORD-1");

        assertThat(result).containsExactly(firstDto, secondDto);
    }
}

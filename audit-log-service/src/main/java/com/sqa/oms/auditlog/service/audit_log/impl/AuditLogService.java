package com.sqa.oms.auditlog.service.audit_log.impl;

import com.sqa.oms.auditlog.dto.AuditLogDTO;
import com.sqa.oms.auditlog.exception.EntityNotFoundException;
import com.sqa.oms.auditlog.mapper.IAuditLogMapper;
import com.sqa.oms.auditlog.modules.audit_log.model.AuditLogSearchRequest;
import com.sqa.oms.auditlog.service.audit_log.IAuditLogService;
import com.sqa.oms.auditlog.service.model.PagedList;
import com.sqa.oms.auditlog.service.store.IAuditLogStore;
import com.sqa.oms.auditlog.service.store.model.AuditLogEntry;
import com.sqa.oms.auditlog.util.Check;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Default implementation of the core audit log read access.
 */
@Service
public final class AuditLogService implements IAuditLogService {

    private final IAuditLogStore auditLogStore;

    private final IAuditLogMapper auditLogMapper;

    /**
     * Constructor with dependency injection.
     *
     * @param auditLogStore  the active audit log store
     * @param auditLogMapper mapper from the store model to the internal DTO
     */
    public AuditLogService(final IAuditLogStore auditLogStore, final IAuditLogMapper auditLogMapper) {
        Check.isNotNull(auditLogStore, "auditLogStore");
        Check.isNotNull(auditLogMapper, "auditLogMapper");

        this.auditLogStore = auditLogStore;
        this.auditLogMapper = auditLogMapper;
    }

    @Override
    public AuditLogDTO findById(final String id) {
        Check.isNotEmpty(id, "id");

        AuditLogEntry entry = auditLogStore.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Audit log entry " + id + " does not exist"));

        return auditLogMapper.toDto(entry);
    }

    @Override
    public PagedList<AuditLogDTO> findPagedList(final AuditLogSearchRequest request, final Pageable pageable) {
        Check.isNotNull(request, "request");
        Check.isNotNull(pageable, "pageable");

        Page<AuditLogEntry> page = auditLogStore.findPaged(request, pageable);
        List<AuditLogDTO> items = page.getContent().stream().map(auditLogMapper::toDto).toList();

        return new PagedList<>(items, page.getTotalElements());
    }

    @Override
    public List<AuditLogDTO> findEntityHistory(final String entityType, final String entityId) {
        Check.isNotEmpty(entityType, "entityType");
        Check.isNotEmpty(entityId, "entityId");

        return auditLogStore.findByEntity(entityType, entityId)
                .stream()
                .map(auditLogMapper::toDto)
                .toList();
    }
}

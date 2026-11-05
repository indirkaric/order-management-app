package com.sqa.oms.auditlog.modules.audit_log.service.impl;

import com.sqa.oms.auditlog.dto.AuditLogDTO;
import com.sqa.oms.auditlog.mapper.IAuditLogMapper;
import com.sqa.oms.auditlog.modules.audit_log.model.AuditLogResponse;
import com.sqa.oms.auditlog.modules.audit_log.service.IAuditLogHistoryResponseCreatorService;
import com.sqa.oms.auditlog.service.audit_log.IAuditLogService;
import com.sqa.oms.auditlog.util.Check;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Default implementation of the entity change history response builder.
 */
@Service
public final class AuditLogHistoryResponseCreatorService implements IAuditLogHistoryResponseCreatorService {

    private final IAuditLogService auditLogService;

    private final IAuditLogMapper auditLogMapper;

    /**
     * Constructor with dependency injection.
     *
     * @param auditLogService core audit log read access
     * @param auditLogMapper  audit log dto and response mapper
     */
    public AuditLogHistoryResponseCreatorService(final IAuditLogService auditLogService,
                                                 final IAuditLogMapper auditLogMapper) {
        Check.isNotNull(auditLogService, "auditLogService");
        Check.isNotNull(auditLogMapper, "auditLogMapper");

        this.auditLogService = auditLogService;
        this.auditLogMapper = auditLogMapper;
    }

    @Override
    public List<AuditLogResponse> create(final String entityType, final String entityId) {
        Check.isNotEmpty(entityType, "entityType");
        Check.isNotEmpty(entityId, "entityId");

        List<AuditLogDTO> entries = auditLogService.findEntityHistory(entityType, entityId);

        return entries.stream().map(auditLogMapper::toResponse).toList();
    }
}

package com.sqa.oms.auditlog.modules.audit_log.service.impl;

import com.sqa.oms.auditlog.dto.AuditLogDTO;
import com.sqa.oms.auditlog.mapper.IAuditLogMapper;
import com.sqa.oms.auditlog.modules.audit_log.model.AuditLogResponse;
import com.sqa.oms.auditlog.modules.audit_log.service.IAuditLogResponseCreatorService;
import com.sqa.oms.auditlog.service.audit_log.IAuditLogService;
import com.sqa.oms.auditlog.util.Check;
import org.springframework.stereotype.Service;

/**
 * Default implementation of the single audit log entry response builder.
 */
@Service
public final class AuditLogResponseCreatorService implements IAuditLogResponseCreatorService {

    private final IAuditLogService auditLogService;

    private final IAuditLogMapper auditLogMapper;

    /**
     * Constructor with dependency injection.
     *
     * @param auditLogService core audit log read access
     * @param auditLogMapper  audit log dto and response mapper
     */
    public AuditLogResponseCreatorService(final IAuditLogService auditLogService,
                                          final IAuditLogMapper auditLogMapper) {
        Check.isNotNull(auditLogService, "auditLogService");
        Check.isNotNull(auditLogMapper, "auditLogMapper");

        this.auditLogService = auditLogService;
        this.auditLogMapper = auditLogMapper;
    }

    @Override
    public AuditLogResponse create(final String id) {
        Check.isNotEmpty(id, "id");

        AuditLogDTO entry = auditLogService.findById(id);

        return auditLogMapper.toResponse(entry);
    }
}

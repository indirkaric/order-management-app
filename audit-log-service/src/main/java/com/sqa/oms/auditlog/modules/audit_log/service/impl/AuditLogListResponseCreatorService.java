package com.sqa.oms.auditlog.modules.audit_log.service.impl;

import com.sqa.oms.auditlog.dto.AuditLogDTO;
import com.sqa.oms.auditlog.mapper.IAuditLogMapper;
import com.sqa.oms.auditlog.modules.audit_log.model.AuditLogListResponse;
import com.sqa.oms.auditlog.modules.audit_log.model.AuditLogResponse;
import com.sqa.oms.auditlog.modules.audit_log.model.AuditLogSearchRequest;
import com.sqa.oms.auditlog.modules.audit_log.service.IAuditLogListResponseCreatorService;
import com.sqa.oms.auditlog.service.audit_log.IAuditLogService;
import com.sqa.oms.auditlog.service.model.PagedList;
import com.sqa.oms.auditlog.util.Check;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Default implementation of the audit log list response builder.
 */
@Service
public final class AuditLogListResponseCreatorService implements IAuditLogListResponseCreatorService {

    private final IAuditLogService auditLogService;

    private final IAuditLogMapper auditLogMapper;

    /**
     * Constructor with dependency injection.
     *
     * @param auditLogService core audit log read access
     * @param auditLogMapper  audit log dto and response mapper
     */
    public AuditLogListResponseCreatorService(final IAuditLogService auditLogService,
                                              final IAuditLogMapper auditLogMapper) {
        Check.isNotNull(auditLogService, "auditLogService");
        Check.isNotNull(auditLogMapper, "auditLogMapper");

        this.auditLogService = auditLogService;
        this.auditLogMapper = auditLogMapper;
    }

    @Override
    public AuditLogListResponse create(final AuditLogSearchRequest request, final Pageable pageable) {
        Check.isNotNull(request, "request");
        Check.isNotNull(pageable, "pageable");

        PagedList<AuditLogDTO> pagedList = auditLogService.findPagedList(request, pageable);
        List<AuditLogResponse> data = pagedList.getItems().stream().map(auditLogMapper::toResponse).toList();

        return new AuditLogListResponse(data, pagedList.getCount());
    }
}

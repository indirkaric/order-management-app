package com.sqa.oms.ui.service.audit.impl;

import com.sqa.oms.ui.client.audit.AuditLogClient;
import com.sqa.oms.ui.constants.Constants;
import com.sqa.oms.ui.modules.audit.model.AuditLogListResponse;
import com.sqa.oms.ui.modules.audit.model.AuditLogResponse;
import com.sqa.oms.ui.modules.audit.model.AuditLogSearchRequest;
import com.sqa.oms.ui.service.audit.IAuditLogService;
import com.sqa.oms.ui.util.Check;
import com.sqa.oms.ui.util.ResponseUtil;
import lombok.SneakyThrows;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Forwards every audit log call to the audit log service.
 */
@Service
public class AuditLogService implements IAuditLogService {

    private final AuditLogClient auditLogClient;

    /**
     * Constructor with dependency injection.
     *
     * @param auditLogClient audit log service client
     */
    public AuditLogService(final AuditLogClient auditLogClient) {
        Check.isNotNull(auditLogClient, "auditLogClient");
        this.auditLogClient = auditLogClient;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SneakyThrows
    public AuditLogListResponse getAll(final AuditLogSearchRequest request, final Pageable pageable) {
        Check.isNotNull(request, "request");
        Check.isNotNull(pageable, "pageable");

        AuditLogListResponse response = ResponseUtil.handleResponse(auditLogClient.getAuditLogs(request, pageable),
                AuditLogListResponse.class, Constants.AUDIT_LOG_SERVICE_NAME);

        return response == null ? new AuditLogListResponse() : response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SneakyThrows
    public AuditLogResponse getById(final String auditLogId) {
        Check.isNotEmpty(auditLogId, "auditLogId");
        return ResponseUtil.handleResponse(auditLogClient.getAuditLog(auditLogId), AuditLogResponse.class,
                Constants.AUDIT_LOG_SERVICE_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SneakyThrows
    public List<AuditLogResponse> getEntityHistory(final String entityType, final String entityId) {
        Check.isNotEmpty(entityType, "entityType");
        Check.isNotEmpty(entityId, "entityId");

        AuditLogResponse[] history = ResponseUtil.handleResponse(auditLogClient.getEntityHistory(entityType,
                entityId), AuditLogResponse[].class, Constants.AUDIT_LOG_SERVICE_NAME);

        return history == null ? List.of() : Arrays.asList(history);
    }
}

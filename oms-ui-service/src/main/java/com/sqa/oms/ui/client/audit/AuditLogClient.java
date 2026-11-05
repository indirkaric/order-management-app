package com.sqa.oms.ui.client.audit;

import com.sqa.oms.ui.client.BaseClient;
import com.sqa.oms.ui.client.response.ClientResponseWrapper;
import com.sqa.oms.ui.constants.Constants;
import com.sqa.oms.ui.modules.audit.model.AuditLogSearchRequest;
import com.sqa.oms.ui.service.security.IScopeContext;
import com.sqa.oms.ui.util.QueryParamsBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Client for the audit log service.
 */
@Service
public class AuditLogClient extends BaseClient {

    private static final String AUDIT_LOGS_PATH = "audit-logs";

    @Value("${client.audit-log.base-uri}")
    private String baseUri;

    /**
     * Constructor with dependency injection.
     *
     * @param webClient    shared WebClient
     * @param scopeContext resolver of the authenticated caller
     */
    public AuditLogClient(final WebClient webClient, final IScopeContext scopeContext) {
        super(webClient, scopeContext);
    }

    /**
     * Retrieves the audit log list.
     *
     * @param request  the query parameters
     * @param pageable paging information
     * @return downstream response
     */
    public ClientResponseWrapper getAuditLogs(final AuditLogSearchRequest request, final Pageable pageable) {
        MultiValueMap<String, String> params = QueryParamsBuilder.create();
        QueryParamsBuilder.addIfPresent(params, "entityType", request.getEntityType());
        QueryParamsBuilder.addIfPresent(params, "entityId", request.getEntityId());
        QueryParamsBuilder.addIfPresent(params, "action", request.getAction());
        QueryParamsBuilder.addIfPresent(params, "performedBy", request.getPerformedBy());
        QueryParamsBuilder.addIfPresent(params, "from", request.getFrom());
        QueryParamsBuilder.addIfPresent(params, "to", request.getTo());
        QueryParamsBuilder.addPaging(params, pageable);

        return get(AUDIT_LOGS_PATH, params);
    }

    /**
     * Retrieves a single audit log entry.
     *
     * @param auditLogId audit log identifier
     * @return downstream response
     */
    public ClientResponseWrapper getAuditLog(final String auditLogId) {
        return get(AUDIT_LOGS_PATH + "/" + auditLogId, null);
    }

    /**
     * Retrieves the full change history of a single entity.
     *
     * @param entityType the entity type
     * @param entityId   the entity identifier
     * @return downstream response
     */
    public ClientResponseWrapper getEntityHistory(final String entityType, final String entityId) {
        return get(AUDIT_LOGS_PATH + "/entity/" + entityType + "/" + entityId, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String baseUri() {
        return baseUri;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String clientId() {
        return Constants.GATEWAY_AUDIT_LOG_CLIENT;
    }
}

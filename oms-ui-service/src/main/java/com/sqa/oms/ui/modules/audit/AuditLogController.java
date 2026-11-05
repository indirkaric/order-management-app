package com.sqa.oms.ui.modules.audit;

import com.sqa.oms.ui.constants.Constants;
import com.sqa.oms.ui.modules.audit.model.AuditLogListResponse;
import com.sqa.oms.ui.modules.audit.model.AuditLogResponse;
import com.sqa.oms.ui.modules.audit.model.AuditLogSearchRequest;
import com.sqa.oms.ui.service.audit.IAuditLogService;
import com.sqa.oms.ui.util.Check;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Proxies the query API of audit-log-service.
 */
@RestController
@RequestMapping(path = "/api/v1/audit-logs", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Audit Log Controller", description = "Proxy to audit-log-service query APIs.")
public class AuditLogController {

    private static final int DEFAULT_PAGE_SIZE = 50;

    private final IAuditLogService auditLogService;

    /**
     * Constructor with dependency injection.
     *
     * @param auditLogService proxy service
     */
    public AuditLogController(final IAuditLogService auditLogService) {
        Check.isNotNull(auditLogService, "auditLogService");
        this.auditLogService = auditLogService;
    }

    /**
     * Searches the audit log.
     *
     * @param request  the query parameters
     * @param pageable the paging information
     * @return a page of audit log entries
     */
    @Operation(
            summary = "Search audit log entries",
            description = "Returns a filtered and paged list of stored data change events",
            security = {@SecurityRequirement(name = Constants.BEARER_KEY_VALUE)})
    @ApiResponses(
            value = {@ApiResponse(
                    responseCode = "200",
                    description = "Page of audit log entries",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuditLogListResponse.class))}),
                    @ApiResponse(responseCode = "204", description = "No audit log entries are found",
                            content = @Content)})
    @GetMapping
    @PreAuthorize(Constants.Authority.AUDIT_READ_AUTHORITY)
    public ResponseEntity<AuditLogListResponse> getAll(
            @ParameterObject final AuditLogSearchRequest request,
            @ParameterObject @PageableDefault(size = DEFAULT_PAGE_SIZE) final Pageable pageable) {
        AuditLogListResponse response = auditLogService.getAll(request, pageable);

        if (response.getData().isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Returns a single audit log entry.
     *
     * @param id audit log identifier
     * @return the audit log entry
     */
    @Operation(
            summary = "Get a single audit log entry",
            description = "Returns one stored data change event",
            security = {@SecurityRequirement(name = Constants.BEARER_KEY_VALUE)})
    @ApiResponses(
            value = {@ApiResponse(
                    responseCode = "200",
                    description = "The audit log entry",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuditLogResponse.class))}),
                    @ApiResponse(responseCode = "404", description = "Entry not found", content = @Content)})
    @GetMapping("/{id}")
    @PreAuthorize(Constants.Authority.AUDIT_READ_AUTHORITY)
    public ResponseEntity<AuditLogResponse> getById(@PathVariable final String id) {
        return ResponseEntity.ok(auditLogService.getById(id));
    }

    /**
     * Returns the full change history of a single entity.
     *
     * @param entityType the entity type
     * @param entityId   the entity identifier
     * @return the ordered change history
     */
    @Operation(
            summary = "Get the change history of one entity",
            description = "Returns every stored data change event of a single entity, oldest first",
            security = {@SecurityRequirement(name = Constants.BEARER_KEY_VALUE)})
    @ApiResponses(
            value = {@ApiResponse(
                    responseCode = "200",
                    description = "The change history of the entity",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuditLogResponse.class))}),
                    @ApiResponse(responseCode = "204", description = "No change history is found",
                            content = @Content)})
    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize(Constants.Authority.AUDIT_READ_AUTHORITY)
    public ResponseEntity<List<AuditLogResponse>> getEntityHistory(@PathVariable final String entityType,
                                                                    @PathVariable final String entityId) {
        List<AuditLogResponse> history = auditLogService.getEntityHistory(entityType, entityId);

        if (history.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(history);
    }
}

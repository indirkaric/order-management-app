package com.sqa.oms.auditlog.modules.audit_log;

import com.sqa.oms.auditlog.constants.Constants;
import com.sqa.oms.auditlog.modules.audit_log.model.AuditLogListResponse;
import com.sqa.oms.auditlog.modules.audit_log.model.AuditLogResponse;
import com.sqa.oms.auditlog.modules.audit_log.model.AuditLogSearchRequest;
import com.sqa.oms.auditlog.modules.audit_log.service.IAuditLogHistoryResponseCreatorService;
import com.sqa.oms.auditlog.modules.audit_log.service.IAuditLogListResponseCreatorService;
import com.sqa.oms.auditlog.modules.audit_log.service.IAuditLogResponseCreatorService;
import com.sqa.oms.auditlog.util.Check;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Query API over the stored audit log.
 */
@RestController
@RequestMapping(path = {"/api/v1/audit-logs"}, produces = APPLICATION_JSON_VALUE)
@Tag(name = "Audit Log Controller", description = "Audit log query APIs.")
public class AuditLogController {

    private final IAuditLogListResponseCreatorService auditLogListResponseCreatorService;

    private final IAuditLogResponseCreatorService auditLogResponseCreatorService;

    private final IAuditLogHistoryResponseCreatorService auditLogHistoryResponseCreatorService;

    /**
     * Constructor with dependency injection.
     *
     * @param auditLogListResponseCreatorService    builds the response of the search endpoint
     * @param auditLogResponseCreatorService        builds the single audit log entry response
     * @param auditLogHistoryResponseCreatorService builds the entity change history response
     */
    public AuditLogController(final IAuditLogListResponseCreatorService auditLogListResponseCreatorService,
                              final IAuditLogResponseCreatorService auditLogResponseCreatorService,
                              final IAuditLogHistoryResponseCreatorService auditLogHistoryResponseCreatorService) {
        Check.isNotNull(auditLogListResponseCreatorService, "auditLogListResponseCreatorService");
        Check.isNotNull(auditLogResponseCreatorService, "auditLogResponseCreatorService");
        Check.isNotNull(auditLogHistoryResponseCreatorService, "auditLogHistoryResponseCreatorService");

        this.auditLogListResponseCreatorService = auditLogListResponseCreatorService;
        this.auditLogResponseCreatorService = auditLogResponseCreatorService;
        this.auditLogHistoryResponseCreatorService = auditLogHistoryResponseCreatorService;
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
                    content = {@Content(mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuditLogListResponse.class))}),
                    @ApiResponse(responseCode = "204", description = "No audit log entries are found",
                            content = @Content)})
    @GetMapping
    @Parameters({
            @Parameter(name = "pageable", hidden = true),
            @Parameter(
                    name = "entityType",
                    schema = @Schema(type = "string"),
                    in = ParameterIn.QUERY,
                    description = "Filter by entity type"),
            @Parameter(
                    name = "entityId",
                    schema = @Schema(type = "string"),
                    in = ParameterIn.QUERY,
                    description = "Filter by entity identifier"),
            @Parameter(
                    name = "action",
                    schema = @Schema(type = "string"),
                    in = ParameterIn.QUERY,
                    description = "Filter by action (CREATE, UPDATE or DELETE)"),
            @Parameter(
                    name = "performedBy",
                    schema = @Schema(type = "string"),
                    in = ParameterIn.QUERY,
                    description = "Filter by the user that performed the change"),
            @Parameter(
                    name = "from",
                    schema = @Schema(type = "string", format = "date-time"),
                    in = ParameterIn.QUERY,
                    description = "Only include events on or after this moment"),
            @Parameter(
                    name = "to",
                    schema = @Schema(type = "string", format = "date-time"),
                    in = ParameterIn.QUERY,
                    description = "Only include events on or before this moment"),
            @Parameter(
                    name = "page",
                    schema = @Schema(type = "integer", example = "0", defaultValue = "0"),
                    in = ParameterIn.QUERY,
                    description = "Page number to retrieve."),
            @Parameter(
                    name = "size",
                    schema = @Schema(type = "integer", example = "20", defaultValue = "20"),
                    in = ParameterIn.QUERY,
                    description = "Page size to retrieve."),
            @Parameter(
                    name = "sort",
                    schema = @Schema(example = "timestamp", defaultValue = ""),
                    in = ParameterIn.QUERY,
                    description = "Name of the field to sort data.")
    })
    @PreAuthorize(Constants.Authority.AUDIT_READ_AUTHORITIES)
    public ResponseEntity<AuditLogListResponse> search(
            final AuditLogSearchRequest request,
            @PageableDefault(size = Constants.DEFAULT_PAGE_SIZE) final Pageable pageable) {
        Check.isNotNull(request, "request");
        Check.isNotNull(pageable, "pageable");

        AuditLogListResponse response = auditLogListResponseCreatorService.create(request, pageable);

        if (response.getData().isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Returns a single audit log entry.
     *
     * @param id the store specific identifier
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
                    content = {@Content(mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuditLogResponse.class))})})
    @GetMapping("/{id}")
    @PreAuthorize(Constants.Authority.AUDIT_READ_AUTHORITIES)
    public ResponseEntity<AuditLogResponse> findById(@PathVariable final String id) {
        Check.isNotEmpty(id, "id");

        return ResponseEntity.ok(auditLogResponseCreatorService.create(id));
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
                    content = {@Content(mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuditLogResponse.class))}),
                    @ApiResponse(responseCode = "204", description = "No change history is found",
                            content = @Content)})
    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize(Constants.Authority.AUDIT_READ_AUTHORITIES)
    public ResponseEntity<List<AuditLogResponse>> findEntityHistory(
            @PathVariable final String entityType,
            @PathVariable final String entityId) {
        Check.isNotEmpty(entityType, "entityType");
        Check.isNotEmpty(entityId, "entityId");

        List<AuditLogResponse> history = auditLogHistoryResponseCreatorService.create(entityType, entityId);

        if (history.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(history);
    }
}

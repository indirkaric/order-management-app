package com.sqa.oms.identity.modules.permission;

import com.sqa.oms.identity.constants.Constants;
import com.sqa.oms.identity.modules.permission.model.PermissionListRequest;
import com.sqa.oms.identity.modules.permission.model.PermissionListResponse;
import com.sqa.oms.identity.modules.permission.model.PermissionResponse;
import com.sqa.oms.identity.modules.permission.service.IPermissionListResponseCreatorService;
import com.sqa.oms.identity.modules.permission.service.IPermissionResponseCreatorService;
import com.sqa.oms.identity.util.Check;
import com.sqa.oms.identity.util.CustomFieldProcessor;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Read only API over permissions. Permissions are seeded by Liquibase and mirrored by
 * Keycloak client scopes of the same name, so they are never created or modified here.
 */
@RestController
@RequestMapping(path = {"/api/v1/permissions"}, produces = APPLICATION_JSON_VALUE)
@Tag(name = "Permission Controller", description = "Permission read APIs.")
public class PermissionController {

    private final IPermissionResponseCreatorService permissionResponseCreatorService;

    private final IPermissionListResponseCreatorService permissionListResponseCreatorService;

    /**
     * Constructor with dependency injection.
     *
     * @param permissionResponseCreatorService     builds the single permission response
     * @param permissionListResponseCreatorService builds the permission list response
     */
    public PermissionController(final IPermissionResponseCreatorService permissionResponseCreatorService,
                                final IPermissionListResponseCreatorService permissionListResponseCreatorService) {
        Check.isNotNull(permissionResponseCreatorService, "permissionResponseCreatorService");
        Check.isNotNull(permissionListResponseCreatorService, "permissionListResponseCreatorService");

        this.permissionResponseCreatorService = permissionResponseCreatorService;
        this.permissionListResponseCreatorService = permissionListResponseCreatorService;
    }

    /**
     * Searches permissions.
     *
     * @param request  the query parameters
     * @param pageable the paging information
     * @return a page of permissions
     */
    @Operation(
            summary = "Search permissions",
            description = "Returns a filtered and paged list of permissions",
            security = {@SecurityRequirement(name = Constants.BEARER_KEY_VALUE)})
    @ApiResponses(
            value = {@ApiResponse(
                    responseCode = "200",
                    description = "Page of permissions",
                    content = {@Content(mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PermissionListResponse.class))}),
                    @ApiResponse(responseCode = "204", description = "No permissions are found",
                            content = @Content)})
    @GetMapping
    @PreAuthorize(Constants.Authority.USER_READ_OR_MANAGE)
    public ResponseEntity<PermissionListResponse> getPermissions(
            @ParameterObject final PermissionListRequest request,
            @ParameterObject @PageableDefault(size = Constants.PAGE_SIZE_DEFAULT_VALUE) final Pageable pageable) {

        Check.isNotNull(request, "request");
        Check.isNotNull(pageable, "pageable");

        CustomFieldProcessor.processAnnotatedFields(request);

        PermissionListResponse response = permissionListResponseCreatorService.create(request, pageable);

        if (response.getData().isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Returns a single permission.
     *
     * @param entityId business key of the permission
     * @return the permission
     */
    @Operation(
            summary = "Get a single permission",
            description = "Returns one permission",
            security = {@SecurityRequirement(name = Constants.BEARER_KEY_VALUE)})
    @ApiResponses(
            value = {@ApiResponse(
                    responseCode = "200",
                    description = "The permission",
                    content = {@Content(mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PermissionResponse.class))}),
                    @ApiResponse(responseCode = "404", description = "Permission not found", content = @Content)})
    @GetMapping("/{id}")
    @PreAuthorize(Constants.Authority.USER_READ_OR_MANAGE)
    public ResponseEntity<PermissionResponse> getPermission(@PathVariable("id") final String entityId) {
        Check.isNotEmpty(entityId, "entityId");

        return ResponseEntity.ok(permissionResponseCreatorService.create(entityId.trim().toUpperCase()));
    }
}

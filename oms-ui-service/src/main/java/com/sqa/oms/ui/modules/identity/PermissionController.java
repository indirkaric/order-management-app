package com.sqa.oms.ui.modules.identity;

import com.sqa.oms.ui.constants.Constants;
import com.sqa.oms.ui.modules.identity.model.PermissionListRequest;
import com.sqa.oms.ui.modules.identity.model.PermissionListResponse;
import com.sqa.oms.ui.modules.identity.model.PermissionResponse;
import com.sqa.oms.ui.service.identity.IPermissionService;
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

/**
 * Proxies the permission resources of identity-service. Permissions are read-only.
 */
@RestController
@RequestMapping(path = "/api/v1/permissions", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Permission Controller", description = "Proxy to identity-service permission APIs.")
public class PermissionController {

    private static final int DEFAULT_PAGE_SIZE = 50;

    private final IPermissionService permissionService;

    /**
     * Constructor with dependency injection.
     *
     * @param permissionService proxy service
     */
    public PermissionController(final IPermissionService permissionService) {
        Check.isNotNull(permissionService, "permissionService");
        this.permissionService = permissionService;
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
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PermissionListResponse.class))}),
                    @ApiResponse(responseCode = "204", description = "No permissions are found",
                            content = @Content)})
    @GetMapping
    @PreAuthorize(Constants.Authority.USER_READ_OR_MANAGE_AUTHORITY)
    public ResponseEntity<PermissionListResponse> getAll(
            @ParameterObject final PermissionListRequest request,
            @ParameterObject @PageableDefault(size = DEFAULT_PAGE_SIZE) final Pageable pageable) {
        PermissionListResponse response = permissionService.getAll(request, pageable);

        if (response.getData().isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Returns a single permission.
     *
     * @param id permission identifier
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
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PermissionResponse.class))}),
                    @ApiResponse(responseCode = "404", description = "Permission not found", content = @Content)})
    @GetMapping("/{id}")
    @PreAuthorize(Constants.Authority.USER_READ_OR_MANAGE_AUTHORITY)
    public ResponseEntity<PermissionResponse> getById(@PathVariable final String id) {
        return ResponseEntity.ok(permissionService.getById(id));
    }
}

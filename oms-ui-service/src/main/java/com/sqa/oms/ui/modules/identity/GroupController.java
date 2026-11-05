package com.sqa.oms.ui.modules.identity;

import com.sqa.oms.ui.constants.Constants;
import com.sqa.oms.ui.modules.identity.model.CreateGroupRequest;
import com.sqa.oms.ui.modules.identity.model.EntityCreatedResponse;
import com.sqa.oms.ui.modules.identity.model.ExpandOption;
import com.sqa.oms.ui.modules.identity.model.GroupListRequest;
import com.sqa.oms.ui.modules.identity.model.GroupListResponse;
import com.sqa.oms.ui.modules.identity.model.GroupResponse;
import com.sqa.oms.ui.modules.identity.model.UpdateGroupRequest;
import com.sqa.oms.ui.service.identity.IGroupService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Proxies the group resources of identity-service.
 */
@RestController
@RequestMapping(path = "/api/v1/groups", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Group Controller", description = "Proxy to identity-service group APIs.")
public class GroupController {

    private static final int DEFAULT_PAGE_SIZE = 50;

    private final IGroupService groupService;

    /**
     * Constructor with dependency injection.
     *
     * @param groupService proxy service
     */
    public GroupController(final IGroupService groupService) {
        Check.isNotNull(groupService, "groupService");
        this.groupService = groupService;
    }

    /**
     * Searches groups.
     *
     * @param request  the query parameters
     * @param pageable the paging information
     * @return a page of groups
     */
    @Operation(
            summary = "Search groups",
            description = "Returns a filtered and paged list of permission groups",
            security = {@SecurityRequirement(name = Constants.BEARER_KEY_VALUE)})
    @ApiResponses(
            value = {@ApiResponse(
                    responseCode = "200",
                    description = "Page of groups",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GroupListResponse.class))}),
                    @ApiResponse(responseCode = "204", description = "No groups are found", content = @Content)})
    @GetMapping
    @PreAuthorize(Constants.Authority.USER_READ_OR_MANAGE_AUTHORITY)
    public ResponseEntity<GroupListResponse> getAll(
            @ParameterObject final GroupListRequest request,
            @ParameterObject @PageableDefault(size = DEFAULT_PAGE_SIZE) final Pageable pageable) {
        GroupListResponse response = groupService.getAll(request, pageable);

        if (response.getData().isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Returns a single group.
     *
     * @param id     group identifier
     * @param expand optional response expansion
     * @return the group
     */
    @Operation(
            summary = "Get a single group",
            description = "Returns one permission group, optionally expanded with its permissions",
            security = {@SecurityRequirement(name = Constants.BEARER_KEY_VALUE)})
    @ApiResponses(
            value = {@ApiResponse(
                    responseCode = "200",
                    description = "The group",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GroupResponse.class))}),
                    @ApiResponse(responseCode = "404", description = "Group not found", content = @Content)})
    @GetMapping("/{id}")
    @PreAuthorize(Constants.Authority.USER_READ_OR_MANAGE_AUTHORITY)
    public ResponseEntity<GroupResponse> getById(@PathVariable final String id,
                                                 @RequestParam(required = false) final ExpandOption expand) {
        return ResponseEntity.ok(groupService.getById(id, expand));
    }

    /**
     * Creates a group.
     *
     * @param request request body
     * @return the business key of the created group
     */
    @Operation(
            summary = "Create a group",
            description = "Creates a permission group and assigns its initial permissions",
            security = {@SecurityRequirement(name = Constants.BEARER_KEY_VALUE)})
    @ApiResponses(
            value = {@ApiResponse(
                    responseCode = "201",
                    description = "Group created",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = EntityCreatedResponse.class))}),
                    @ApiResponse(responseCode = "422", description = "Invalid input", content = @Content)})
    @PostMapping
    @PreAuthorize(Constants.Authority.USER_MANAGE_AUTHORITY)
    public ResponseEntity<EntityCreatedResponse> create(@RequestBody final CreateGroupRequest request) {
        return new ResponseEntity<>(groupService.create(request), HttpStatus.CREATED);
    }

    /**
     * Updates a group and replaces its whole set of permission assignments.
     *
     * @param id      group identifier
     * @param request request body
     * @return no content
     */
    @Operation(
            summary = "Update a group",
            description = "Updates a permission group and replaces its whole set of permission assignments",
            security = {@SecurityRequirement(name = Constants.BEARER_KEY_VALUE)})
    @ApiResponses(
            value = {@ApiResponse(responseCode = "204", description = "Group updated", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Group not found", content = @Content),
                    @ApiResponse(responseCode = "422", description = "Invalid input", content = @Content)})
    @PutMapping("/{id}")
    @PreAuthorize(Constants.Authority.USER_MANAGE_AUTHORITY)
    public ResponseEntity<Void> update(@PathVariable final String id,
                                       @RequestBody final UpdateGroupRequest request) {
        groupService.update(id, request);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes a group.
     *
     * @param id group identifier
     * @return no content
     */
    @Operation(
            summary = "Delete a group",
            description = "Deletes a group",
            security = {@SecurityRequirement(name = Constants.BEARER_KEY_VALUE)})
    @ApiResponses(
            value = {@ApiResponse(responseCode = "204", description = "Group deleted", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Group not found", content = @Content)})
    @DeleteMapping("/{id}")
    @PreAuthorize(Constants.Authority.USER_MANAGE_AUTHORITY)
    public ResponseEntity<Void> delete(@PathVariable final String id) {
        groupService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

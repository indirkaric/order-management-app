package com.sqa.oms.identity.modules.group;

import com.sqa.oms.identity.constants.Constants;
import com.sqa.oms.identity.dto.response.EntityCreatedResponse;
import com.sqa.oms.identity.enums.ExpandOption;
import com.sqa.oms.identity.modules.group.model.CreateGroupRequest;
import com.sqa.oms.identity.modules.group.model.GroupListRequest;
import com.sqa.oms.identity.modules.group.model.GroupListResponse;
import com.sqa.oms.identity.modules.group.model.GroupResponse;
import com.sqa.oms.identity.modules.group.model.UpdateGroupRequest;
import com.sqa.oms.identity.modules.group.service.ICreateGroupResponseCreatorService;
import com.sqa.oms.identity.modules.group.service.IGroupListResponseCreatorService;
import com.sqa.oms.identity.modules.group.service.IGroupResponseCreatorService;
import com.sqa.oms.identity.service.group.IModifyGroupService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * CRUD API over permission groups and their permission assignments.
 */
@RestController
@RequestMapping(path = {"/api/v1/groups"}, produces = APPLICATION_JSON_VALUE)
@Tag(name = "Group Controller", description = "Permission group CRUD APIs.")
public class GroupController {

    private final ICreateGroupResponseCreatorService createGroupResponseCreatorService;

    private final IGroupResponseCreatorService groupResponseCreatorService;

    private final IGroupListResponseCreatorService groupListResponseCreatorService;

    private final IModifyGroupService modifyGroupService;

    /**
     * Constructor with dependency injection.
     *
     * @param createGroupResponseCreatorService builds the create response
     * @param groupResponseCreatorService       builds the single group response
     * @param groupListResponseCreatorService   builds the group list response
     * @param modifyGroupService                core group update business logic
     */
    public GroupController(final ICreateGroupResponseCreatorService createGroupResponseCreatorService,
                           final IGroupResponseCreatorService groupResponseCreatorService,
                           final IGroupListResponseCreatorService groupListResponseCreatorService,
                           final IModifyGroupService modifyGroupService) {

        Check.isNotNull(createGroupResponseCreatorService, "createGroupResponseCreatorService");
        Check.isNotNull(groupResponseCreatorService, "groupResponseCreatorService");
        Check.isNotNull(groupListResponseCreatorService, "groupListResponseCreatorService");
        Check.isNotNull(modifyGroupService, "modifyGroupService");

        this.createGroupResponseCreatorService = createGroupResponseCreatorService;
        this.groupResponseCreatorService = groupResponseCreatorService;
        this.groupListResponseCreatorService = groupListResponseCreatorService;
        this.modifyGroupService = modifyGroupService;
    }

    /**
     * Searches permission groups.
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
                    content = {@Content(mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GroupListResponse.class))}),
                    @ApiResponse(responseCode = "204", description = "No groups are found", content = @Content)})
    @GetMapping
    @PreAuthorize(Constants.Authority.USER_READ_OR_MANAGE)
    public ResponseEntity<GroupListResponse> getGroups(
            @ParameterObject final GroupListRequest request,
            @ParameterObject @PageableDefault(size = Constants.PAGE_SIZE_DEFAULT_VALUE) final Pageable pageable) {

        Check.isNotNull(request, "request");
        Check.isNotNull(pageable, "pageable");

        CustomFieldProcessor.processAnnotatedFields(request);

        GroupListResponse response = groupListResponseCreatorService.create(request, pageable);

        if (response.getData().isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Returns a single permission group.
     *
     * @param entityId business key of the group
     * @param expand   optional response expansion
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
                    content = {@Content(mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GroupResponse.class))}),
                    @ApiResponse(responseCode = "404", description = "Group not found", content = @Content)})
    @GetMapping("/{id}")
    @PreAuthorize(Constants.Authority.USER_READ_OR_MANAGE)
    public ResponseEntity<GroupResponse> getGroup(
            @PathVariable("id") final String entityId,
            @RequestParam(required = false) final ExpandOption expand) {

        Check.isNotEmpty(entityId, "entityId");

        return ResponseEntity.ok(groupResponseCreatorService.create(entityId.trim().toUpperCase(), expand));
    }

    /**
     * Creates a permission group.
     *
     * @param request the create request
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
                    content = {@Content(mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = EntityCreatedResponse.class))}),
                    @ApiResponse(responseCode = "422", description = "Invalid input", content = @Content)})
    @PostMapping
    @PreAuthorize(Constants.Authority.USER_MANAGE)
    public ResponseEntity<EntityCreatedResponse> create(@RequestBody final CreateGroupRequest request) {

        Check.isNotNull(request, "request");

        CustomFieldProcessor.processAnnotatedFields(request);

        EntityCreatedResponse response = createGroupResponseCreatorService.create(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Updates a permission group and replaces its whole set of permission assignments.
     *
     * @param entityId business key of the group
     * @param request  the update request
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
    @PreAuthorize(Constants.Authority.USER_MANAGE)
    public ResponseEntity<Void> update(@PathVariable("id") final String entityId,
                                             @RequestBody final UpdateGroupRequest request) {

        Check.isNotEmpty(entityId, "entityId");
        Check.isNotNull(request, "request");

        CustomFieldProcessor.processAnnotatedFields(request);

        modifyGroupService.update(entityId.trim().toUpperCase(), request);

        return ResponseEntity.noContent().build();
    }
}

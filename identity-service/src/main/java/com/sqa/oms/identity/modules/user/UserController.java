package com.sqa.oms.identity.modules.user;

import com.sqa.oms.identity.constants.Constants;
import com.sqa.oms.identity.dto.response.EntityCreatedResponse;
import com.sqa.oms.identity.enums.ExpandOption;
import com.sqa.oms.identity.modules.user.model.CreateUserRequest;
import com.sqa.oms.identity.modules.user.model.ModifyUserRequest;
import com.sqa.oms.identity.modules.user.model.UserListRequest;
import com.sqa.oms.identity.modules.user.model.UserListResponse;
import com.sqa.oms.identity.modules.user.model.UserResponse;
import com.sqa.oms.identity.modules.user.service.ICreateUserResponseCreatorService;
import com.sqa.oms.identity.modules.user.service.IUserListResponseCreatorService;
import com.sqa.oms.identity.modules.user.service.IUserResponseCreatorService;
import com.sqa.oms.identity.service.user.IModifyUserService;
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
 * CRUD API over users and their group memberships.
 */
@RestController
@RequestMapping(path = {"/api/v1/users"}, produces = APPLICATION_JSON_VALUE)
@Tag(name = "User Controller", description = "User CRUD APIs.")
public class UserController {

    private final ICreateUserResponseCreatorService createUserResponseCreatorService;

    private final IUserResponseCreatorService userResponseCreatorService;

    private final IUserListResponseCreatorService userListResponseCreatorService;

    private final IModifyUserService modifyUserService;

    /**
     * Constructor with dependency injection.
     *
     * @param createUserResponseCreatorService builds the create response
     * @param userResponseCreatorService       builds the single user response
     * @param userListResponseCreatorService   builds the user list response
     * @param modifyUserService                core user update business logic
     */
    public UserController(final ICreateUserResponseCreatorService createUserResponseCreatorService,
                          final IUserResponseCreatorService userResponseCreatorService,
                          final IUserListResponseCreatorService userListResponseCreatorService,
                          final IModifyUserService modifyUserService) {

        Check.isNotNull(createUserResponseCreatorService, "createUserResponseCreatorService");
        Check.isNotNull(userResponseCreatorService, "userResponseCreatorService");
        Check.isNotNull(userListResponseCreatorService, "userListResponseCreatorService");
        Check.isNotNull(modifyUserService, "modifyUserService");

        this.createUserResponseCreatorService = createUserResponseCreatorService;
        this.userResponseCreatorService = userResponseCreatorService;
        this.userListResponseCreatorService = userListResponseCreatorService;
        this.modifyUserService = modifyUserService;
    }

    /**
     * Searches users.
     *
     * @param request  the query parameters
     * @param pageable the paging information
     * @return a page of users
     */
    @Operation(
            summary = "Search users",
            description = "Returns a filtered and paged list of users",
            security = {@SecurityRequirement(name = Constants.BEARER_KEY_VALUE)})
    @ApiResponses(
            value = {@ApiResponse(
                    responseCode = "200",
                    description = "Page of users",
                    content = {@Content(mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserListResponse.class))}),
                    @ApiResponse(responseCode = "204", description = "No users are found", content = @Content)})
    @GetMapping
    @PreAuthorize(Constants.Authority.USER_READ_OR_MANAGE)
    public ResponseEntity<UserListResponse> getUsers(
            @ParameterObject final UserListRequest request,
            @ParameterObject @PageableDefault(size = Constants.PAGE_SIZE_DEFAULT_VALUE) final Pageable pageable) {

        Check.isNotNull(request, "request");
        Check.isNotNull(pageable, "pageable");

        CustomFieldProcessor.processAnnotatedFields(request);

        UserListResponse response = userListResponseCreatorService.create(request, pageable);

        if (response.getData().isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Returns a single user.
     *
     * @param entityId business key of the user
     * @param expand   optional response expansion
     * @return the user
     */
    @Operation(
            summary = "Get a single user",
            description = "Returns one user, optionally expanded with its group memberships",
            security = {@SecurityRequirement(name = Constants.BEARER_KEY_VALUE)})
    @ApiResponses(
            value = {@ApiResponse(
                    responseCode = "200",
                    description = "The user",
                    content = {@Content(mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponse.class))}),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content)})
    @GetMapping("/{id}")
    @PreAuthorize(Constants.Authority.USER_READ_OR_MANAGE)
    public ResponseEntity<UserResponse> getUser(
            @PathVariable("id") final String entityId,
            @RequestParam(required = false) final ExpandOption expand) {

        Check.isNotEmpty(entityId, "entityId");

        return ResponseEntity.ok(userResponseCreatorService.create(entityId.trim().toUpperCase(), expand));
    }

    /**
     * Creates a user.
     *
     * @param request the create request
     * @return the business key of the created user
     */
    @Operation(
            summary = "Create a user",
            description = "Creates a user and assigns its initial group memberships",
            security = {@SecurityRequirement(name = Constants.BEARER_KEY_VALUE)})
    @ApiResponses(
            value = {@ApiResponse(
                    responseCode = "201",
                    description = "User created",
                    content = {@Content(mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = EntityCreatedResponse.class))}),
                    @ApiResponse(responseCode = "422", description = "Invalid input", content = @Content)})
    @PostMapping
    @PreAuthorize(Constants.Authority.USER_MANAGE)
    public ResponseEntity<EntityCreatedResponse> create(@RequestBody final CreateUserRequest request) {

        Check.isNotNull(request, "request");

        CustomFieldProcessor.processAnnotatedFields(request);

        EntityCreatedResponse response = createUserResponseCreatorService.create(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Updates a user and replaces its whole set of group memberships.
     *
     * @param entityId business key of the user
     * @param request  the modify request
     * @return no content
     */
    @Operation(
            summary = "Update a user",
            description = "Updates a user and replaces its whole set of group memberships",
            security = {@SecurityRequirement(name = Constants.BEARER_KEY_VALUE)})
    @ApiResponses(
            value = {@ApiResponse(responseCode = "204", description = "User updated", content = @Content),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
                    @ApiResponse(responseCode = "422", description = "Invalid input", content = @Content)})
    @PutMapping("/{id}")
    @PreAuthorize(Constants.Authority.USER_MANAGE)
    public ResponseEntity<Void> modify(@PathVariable("id") final String entityId,
                                             @RequestBody final ModifyUserRequest request) {

        Check.isNotEmpty(entityId, "entityId");
        Check.isNotNull(request, "request");

        CustomFieldProcessor.processAnnotatedFields(request);

        modifyUserService.update(entityId.trim().toUpperCase(), request);

        return ResponseEntity.noContent().build();
    }
}

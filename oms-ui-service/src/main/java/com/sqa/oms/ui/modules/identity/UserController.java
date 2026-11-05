package com.sqa.oms.ui.modules.identity;

import com.sqa.oms.ui.constants.Constants;
import com.sqa.oms.ui.modules.identity.model.CreateUserRequest;
import com.sqa.oms.ui.modules.identity.model.EntityCreatedResponse;
import com.sqa.oms.ui.modules.identity.model.ExpandOption;
import com.sqa.oms.ui.modules.identity.model.ModifyUserRequest;
import com.sqa.oms.ui.modules.identity.model.UserListRequest;
import com.sqa.oms.ui.modules.identity.model.UserListResponse;
import com.sqa.oms.ui.modules.identity.model.UserResponse;
import com.sqa.oms.ui.service.identity.IUserService;
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
 * Proxies the user resources of identity-service.
 */
@RestController
@RequestMapping(path = "/api/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "User Controller", description = "Proxy to identity-service user APIs.")
public class UserController {

    private static final int DEFAULT_PAGE_SIZE = 50;

    private final IUserService userService;

    /**
     * Constructor with dependency injection.
     *
     * @param userService proxy service
     */
    public UserController(final IUserService userService) {
        Check.isNotNull(userService, "userService");
        this.userService = userService;
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
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserListResponse.class))}),
                    @ApiResponse(responseCode = "204", description = "No users are found", content = @Content)})
    @GetMapping
    @PreAuthorize(Constants.Authority.USER_READ_OR_MANAGE_AUTHORITY)
    public ResponseEntity<UserListResponse> getAll(
            @ParameterObject final UserListRequest request,
            @ParameterObject @PageableDefault(size = DEFAULT_PAGE_SIZE) final Pageable pageable) {
        UserListResponse response = userService.getAll(request, pageable);

        if (response.getData().isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Returns a single user.
     *
     * @param id     user identifier
     * @param expand optional response expansion
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
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponse.class))}),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content)})
    @GetMapping("/{id}")
    @PreAuthorize(Constants.Authority.USER_READ_OR_MANAGE_AUTHORITY)
    public ResponseEntity<UserResponse> getById(@PathVariable final String id,
                                                @RequestParam(required = false) final ExpandOption expand) {
        return ResponseEntity.ok(userService.getById(id, expand));
    }

    /**
     * Creates a user.
     *
     * @param request request body
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
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = EntityCreatedResponse.class))}),
                    @ApiResponse(responseCode = "422", description = "Invalid input", content = @Content)})
    @PostMapping
    @PreAuthorize(Constants.Authority.USER_MANAGE_AUTHORITY)
    public ResponseEntity<EntityCreatedResponse> create(@RequestBody final CreateUserRequest request) {
        return new ResponseEntity<>(userService.create(request), HttpStatus.CREATED);
    }

    /**
     * Updates a user and replaces its whole set of group memberships.
     *
     * @param id      user identifier
     * @param request request body
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
    @PreAuthorize(Constants.Authority.USER_MANAGE_AUTHORITY)
    public ResponseEntity<Void> update(@PathVariable final String id,
                                       @RequestBody final ModifyUserRequest request) {
        userService.update(id, request);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes a user.
     *
     * @param id user identifier
     * @return no content
     */
    @Operation(
            summary = "Delete a user",
            description = "Deletes a user",
            security = {@SecurityRequirement(name = Constants.BEARER_KEY_VALUE)})
    @ApiResponses(
            value = {@ApiResponse(responseCode = "204", description = "User deleted", content = @Content),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content)})
    @DeleteMapping("/{id}")
    @PreAuthorize(Constants.Authority.USER_MANAGE_AUTHORITY)
    public ResponseEntity<Void> delete(@PathVariable final String id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

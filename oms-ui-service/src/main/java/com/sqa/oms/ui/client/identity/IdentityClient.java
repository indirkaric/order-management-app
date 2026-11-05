package com.sqa.oms.ui.client.identity;

import com.sqa.oms.ui.client.BaseClient;
import com.sqa.oms.ui.client.response.ClientResponseWrapper;
import com.sqa.oms.ui.constants.Constants;
import com.sqa.oms.ui.modules.identity.model.ExpandOption;
import com.sqa.oms.ui.modules.identity.model.GroupListRequest;
import com.sqa.oms.ui.modules.identity.model.PermissionListRequest;
import com.sqa.oms.ui.modules.identity.model.UserListRequest;
import com.sqa.oms.ui.service.security.IScopeContext;
import com.sqa.oms.ui.util.QueryParamsBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Client for the user, group and permission resources of the identity service.
 */
@Service
public class IdentityClient extends BaseClient {

    private static final String USERS_PATH = "users";

    private static final String GROUPS_PATH = "groups";

    private static final String PERMISSIONS_PATH = "permissions";

    @Value("${client.identity.base-uri}")
    private String baseUri;

    /**
     * Constructor with dependency injection.
     *
     * @param webClient    shared WebClient
     * @param scopeContext resolver of the authenticated caller
     */
    public IdentityClient(final WebClient webClient, final IScopeContext scopeContext) {
        super(webClient, scopeContext);
    }

    /**
     * Retrieves the user list.
     *
     * @param request  the query parameters
     * @param pageable paging information
     * @return downstream response
     */
    public ClientResponseWrapper getUsers(final UserListRequest request, final Pageable pageable) {
        MultiValueMap<String, String> params = QueryParamsBuilder.create();
        QueryParamsBuilder.addAllIfPresent(params, "ids", request.getIds());
        QueryParamsBuilder.addIfPresent(params, "email", request.getEmail());
        QueryParamsBuilder.addIfPresent(params, "status", request.getStatus());
        QueryParamsBuilder.addIfPresent(params, "textSearch", request.getTextSearch());
        QueryParamsBuilder.addAllIfPresent(params, "groupIds", request.getGroupIds());
        QueryParamsBuilder.addIfPresent(params, "expand", request.getExpand());
        QueryParamsBuilder.addPaging(params, pageable);

        return get(USERS_PATH, params);
    }

    /**
     * Retrieves a single user.
     *
     * @param userId user identifier
     * @param expand optional response expansion
     * @return downstream response
     */
    public ClientResponseWrapper getUser(final String userId, final ExpandOption expand) {
        MultiValueMap<String, String> params = QueryParamsBuilder.create();
        QueryParamsBuilder.addIfPresent(params, "expand", expand);

        return get(USERS_PATH + "/" + userId, params);
    }

    /**
     * Creates a user.
     *
     * @param body request body
     * @return downstream response
     */
    public ClientResponseWrapper createUser(final Object body) {
        return send(HttpMethod.POST, USERS_PATH, body);
    }

    /**
     * Updates a user.
     *
     * @param userId user identifier
     * @param body   request body
     * @return downstream response
     */
    public ClientResponseWrapper updateUser(final String userId, final Object body) {
        return send(HttpMethod.PUT, USERS_PATH + "/" + userId, body);
    }

    /**
     * Deletes a user.
     *
     * @param userId user identifier
     * @return downstream response
     */
    public ClientResponseWrapper deleteUser(final String userId) {
        return send(HttpMethod.DELETE, USERS_PATH + "/" + userId, null);
    }

    /**
     * Retrieves the group list.
     *
     * @param request  the query parameters
     * @param pageable paging information
     * @return downstream response
     */
    public ClientResponseWrapper getGroups(final GroupListRequest request, final Pageable pageable) {
        MultiValueMap<String, String> params = QueryParamsBuilder.create();
        QueryParamsBuilder.addAllIfPresent(params, "ids", request.getIds());
        QueryParamsBuilder.addIfPresent(params, "status", request.getStatus());
        QueryParamsBuilder.addIfPresent(params, "textSearch", request.getTextSearch());
        QueryParamsBuilder.addIfPresent(params, "expand", request.getExpand());
        QueryParamsBuilder.addPaging(params, pageable);

        return get(GROUPS_PATH, params);
    }

    /**
     * Retrieves a single group.
     *
     * @param groupId group identifier
     * @param expand  optional response expansion
     * @return downstream response
     */
    public ClientResponseWrapper getGroup(final String groupId, final ExpandOption expand) {
        MultiValueMap<String, String> params = QueryParamsBuilder.create();
        QueryParamsBuilder.addIfPresent(params, "expand", expand);

        return get(GROUPS_PATH + "/" + groupId, params);
    }

    /**
     * Creates a group.
     *
     * @param body request body
     * @return downstream response
     */
    public ClientResponseWrapper createGroup(final Object body) {
        return send(HttpMethod.POST, GROUPS_PATH, body);
    }

    /**
     * Updates a group.
     *
     * @param groupId group identifier
     * @param body    request body
     * @return downstream response
     */
    public ClientResponseWrapper updateGroup(final String groupId, final Object body) {
        return send(HttpMethod.PUT, GROUPS_PATH + "/" + groupId, body);
    }

    /**
     * Deletes a group.
     *
     * @param groupId group identifier
     * @return downstream response
     */
    public ClientResponseWrapper deleteGroup(final String groupId) {
        return send(HttpMethod.DELETE, GROUPS_PATH + "/" + groupId, null);
    }

    /**
     * Retrieves the permission list.
     *
     * @param request  the query parameters
     * @param pageable paging information
     * @return downstream response
     */
    public ClientResponseWrapper getPermissions(final PermissionListRequest request, final Pageable pageable) {
        MultiValueMap<String, String> params = QueryParamsBuilder.create();
        QueryParamsBuilder.addAllIfPresent(params, "ids", request.getIds());
        QueryParamsBuilder.addIfPresent(params, "status", request.getStatus());
        QueryParamsBuilder.addIfPresent(params, "textSearch", request.getTextSearch());
        QueryParamsBuilder.addPaging(params, pageable);

        return get(PERMISSIONS_PATH, params);
    }

    /**
     * Retrieves a single permission.
     *
     * @param permissionId permission identifier
     * @return downstream response
     */
    public ClientResponseWrapper getPermission(final String permissionId) {
        return get(PERMISSIONS_PATH + "/" + permissionId, null);
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
        return Constants.GATEWAY_IDENTITY_CLIENT;
    }
}

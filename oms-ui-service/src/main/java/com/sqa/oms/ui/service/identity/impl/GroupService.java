package com.sqa.oms.ui.service.identity.impl;

import com.sqa.oms.ui.client.identity.IdentityClient;
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
import com.sqa.oms.ui.util.ResponseUtil;
import lombok.SneakyThrows;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Forwards every group call to the identity service.
 */
@Service
public class GroupService implements IGroupService {

    private final IdentityClient identityClient;

    /**
     * Constructor with dependency injection.
     *
     * @param identityClient identity service client
     */
    public GroupService(final IdentityClient identityClient) {
        Check.isNotNull(identityClient, "identityClient");
        this.identityClient = identityClient;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SneakyThrows
    public GroupListResponse getAll(final GroupListRequest request, final Pageable pageable) {
        Check.isNotNull(request, "request");
        Check.isNotNull(pageable, "pageable");

        GroupListResponse response = ResponseUtil.handleResponse(identityClient.getGroups(request, pageable),
                GroupListResponse.class, Constants.IDENTITY_SERVICE_NAME);

        return response == null ? new GroupListResponse() : response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SneakyThrows
    public GroupResponse getById(final String groupId, final ExpandOption expand) {
        Check.isNotEmpty(groupId, "groupId");

        return ResponseUtil.handleResponse(identityClient.getGroup(groupId, expand), GroupResponse.class,
                Constants.IDENTITY_SERVICE_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SneakyThrows
    public EntityCreatedResponse create(final CreateGroupRequest request) {
        Check.isNotNull(request, "request");
        return ResponseUtil.handleResponse(identityClient.createGroup(request), EntityCreatedResponse.class,
                Constants.IDENTITY_SERVICE_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SneakyThrows
    public void update(final String groupId, final UpdateGroupRequest request) {
        Check.isNotEmpty(groupId, "groupId");
        Check.isNotNull(request, "request");
        ResponseUtil.handleResponse(identityClient.updateGroup(groupId, request), Void.class,
                Constants.IDENTITY_SERVICE_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SneakyThrows
    public void delete(final String groupId) {
        Check.isNotEmpty(groupId, "groupId");
        ResponseUtil.handleResponse(identityClient.deleteGroup(groupId), Void.class,
                Constants.IDENTITY_SERVICE_NAME);
    }
}

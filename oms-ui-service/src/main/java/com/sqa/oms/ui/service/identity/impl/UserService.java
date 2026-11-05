package com.sqa.oms.ui.service.identity.impl;

import com.sqa.oms.ui.client.identity.IdentityClient;
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
import com.sqa.oms.ui.util.ResponseUtil;
import lombok.SneakyThrows;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Forwards every user call to the identity service.
 */
@Service
public class UserService implements IUserService {

    private final IdentityClient identityClient;

    /**
     * Constructor with dependency injection.
     *
     * @param identityClient identity service client
     */
    public UserService(final IdentityClient identityClient) {
        Check.isNotNull(identityClient, "identityClient");
        this.identityClient = identityClient;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SneakyThrows
    public UserListResponse getAll(final UserListRequest request, final Pageable pageable) {
        Check.isNotNull(request, "request");
        Check.isNotNull(pageable, "pageable");

        UserListResponse response = ResponseUtil.handleResponse(identityClient.getUsers(request, pageable),
                UserListResponse.class, Constants.IDENTITY_SERVICE_NAME);

        return response == null ? new UserListResponse() : response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SneakyThrows
    public UserResponse getById(final String userId, final ExpandOption expand) {
        Check.isNotEmpty(userId, "userId");

        return ResponseUtil.handleResponse(identityClient.getUser(userId, expand), UserResponse.class,
                Constants.IDENTITY_SERVICE_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SneakyThrows
    public EntityCreatedResponse create(final CreateUserRequest request) {
        Check.isNotNull(request, "request");
        return ResponseUtil.handleResponse(identityClient.createUser(request), EntityCreatedResponse.class,
                Constants.IDENTITY_SERVICE_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SneakyThrows
    public void update(final String userId, final ModifyUserRequest request) {
        Check.isNotEmpty(userId, "userId");
        Check.isNotNull(request, "request");
        ResponseUtil.handleResponse(identityClient.updateUser(userId, request), Void.class,
                Constants.IDENTITY_SERVICE_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SneakyThrows
    public void delete(final String userId) {
        Check.isNotEmpty(userId, "userId");
        ResponseUtil.handleResponse(identityClient.deleteUser(userId), Void.class,
                Constants.IDENTITY_SERVICE_NAME);
    }
}

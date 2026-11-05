package com.sqa.oms.ui.service.identity.impl;

import com.sqa.oms.ui.client.identity.IdentityClient;
import com.sqa.oms.ui.constants.Constants;
import com.sqa.oms.ui.modules.identity.model.PermissionListRequest;
import com.sqa.oms.ui.modules.identity.model.PermissionListResponse;
import com.sqa.oms.ui.modules.identity.model.PermissionResponse;
import com.sqa.oms.ui.service.identity.IPermissionService;
import com.sqa.oms.ui.util.Check;
import com.sqa.oms.ui.util.ResponseUtil;
import lombok.SneakyThrows;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Forwards every permission call to the identity service.
 */
@Service
public class PermissionService implements IPermissionService {

    private final IdentityClient identityClient;

    /**
     * Constructor with dependency injection.
     *
     * @param identityClient identity service client
     */
    public PermissionService(final IdentityClient identityClient) {
        Check.isNotNull(identityClient, "identityClient");
        this.identityClient = identityClient;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SneakyThrows
    public PermissionListResponse getAll(final PermissionListRequest request, final Pageable pageable) {
        Check.isNotNull(request, "request");
        Check.isNotNull(pageable, "pageable");

        PermissionListResponse response = ResponseUtil.handleResponse(identityClient.getPermissions(request,
                pageable), PermissionListResponse.class, Constants.IDENTITY_SERVICE_NAME);

        return response == null ? new PermissionListResponse() : response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SneakyThrows
    public PermissionResponse getById(final String permissionId) {
        Check.isNotEmpty(permissionId, "permissionId");
        return ResponseUtil.handleResponse(identityClient.getPermission(permissionId), PermissionResponse.class,
                Constants.IDENTITY_SERVICE_NAME);
    }
}

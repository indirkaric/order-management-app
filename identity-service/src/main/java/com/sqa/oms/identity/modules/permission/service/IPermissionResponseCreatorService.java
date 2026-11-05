package com.sqa.oms.identity.modules.permission.service;

import com.sqa.oms.identity.modules.permission.model.PermissionResponse;

/**
 * Builds the response of the single permission endpoint.
 */
public interface IPermissionResponseCreatorService {

    /**
     * Returns a single permission.
     *
     * @param entityId business key of the permission
     * @return PermissionResponse
     */
    PermissionResponse create(String entityId);
}

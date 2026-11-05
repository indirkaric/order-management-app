package com.sqa.oms.ui.service.identity;

import com.sqa.oms.ui.modules.identity.model.PermissionListRequest;
import com.sqa.oms.ui.modules.identity.model.PermissionListResponse;
import com.sqa.oms.ui.modules.identity.model.PermissionResponse;
import org.springframework.data.domain.Pageable;

/**
 * Proxy to the permission resources of the identity service.
 */
public interface IPermissionService {

    /**
     * Retrieves the permission list.
     *
     * @param request  list filters
     * @param pageable paging information
     * @return permission list
     */
    PermissionListResponse getAll(PermissionListRequest request, Pageable pageable);

    /**
     * Retrieves a single permission.
     *
     * @param permissionId permission identifier
     * @return the permission
     */
    PermissionResponse getById(String permissionId);
}

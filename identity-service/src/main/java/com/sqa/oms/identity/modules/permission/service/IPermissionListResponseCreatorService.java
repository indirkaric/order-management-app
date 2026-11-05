package com.sqa.oms.identity.modules.permission.service;

import com.sqa.oms.identity.modules.permission.model.PermissionListRequest;
import com.sqa.oms.identity.modules.permission.model.PermissionListResponse;
import org.springframework.data.domain.Pageable;

/**
 * Builds the response of the permission list endpoint.
 */
public interface IPermissionListResponseCreatorService {

    /**
     * Returns the filtered and paged list of permissions.
     *
     * @param request  list filters
     * @param pageable paging and sorting
     * @return PermissionListResponse
     */
    PermissionListResponse create(PermissionListRequest request, Pageable pageable);
}

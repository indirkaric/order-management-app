package com.sqa.oms.identity.service.permission;

import com.sqa.oms.identity.dto.PermissionDTO;
import com.sqa.oms.identity.modules.permission.model.PermissionListRequest;
import com.sqa.oms.identity.service.model.PagedList;
import org.springframework.data.domain.Pageable;

/**
 * Read access to permissions. Permissions are seeded by Liquibase and never created or modified
 * through the API.
 */
public interface IPermissionService {

    /**
     * Returns a single permission.
     *
     * @param entityId business key of the permission
     * @return the matching permission
     */
    PermissionDTO getByEntityId(String entityId);

    /**
     * Returns the filtered and paged list of permissions.
     *
     * @param request  list filters
     * @param pageable paging and sorting
     * @return the matching page of permissions
     */
    PagedList<PermissionDTO> getPagedList(PermissionListRequest request, Pageable pageable);
}

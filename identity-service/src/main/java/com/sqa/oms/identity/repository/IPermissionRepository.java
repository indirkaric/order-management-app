package com.sqa.oms.identity.repository;

import com.sqa.oms.identity.entity.Permission;
import com.sqa.oms.identity.modules.permission.model.PermissionListRequest;
import com.sqa.oms.identity.repository.projection.PermissionProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Domain facing access to the permission table.
 */
public interface IPermissionRepository {

    /**
     * Finds the projection of a permission by its business key.
     *
     * @param entityId business key
     * @return the matching permission projection
     */
    Optional<PermissionProjection> findProjectionByEntityId(String entityId);

    /**
     * Finds all permissions matching the given business keys.
     *
     * @param entityIds business keys
     * @return the matching permissions
     */
    List<Permission> findAllByEntityIdIn(List<String> entityIds);

    /**
     * Returns the filtered and paged list of permission projections.
     *
     * @param request  list filters
     * @param pageable paging and sorting
     * @return the matching page of permission projections
     */
    Page<PermissionProjection> findPagedList(PermissionListRequest request, Pageable pageable);

    /**
     * Returns the names of every permission assigned to a group.
     *
     * @param groupEntityId business key of the group
     * @return the matching permission names
     */
    List<String> findNamesByGroupEntityId(String groupEntityId);
}

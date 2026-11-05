package com.sqa.oms.identity.repository;

import com.sqa.oms.identity.entity.Group;
import com.sqa.oms.identity.entity.GroupPermission;

import java.util.List;

/**
 * Domain facing access to the group_permission join table.
 */
public interface IGroupPermissionRepository {

    /**
     * Finds every permission assignment of a group.
     *
     * @param group owner of the assignments
     * @return the matching rows
     */
    List<GroupPermission> findByGroup(Group group);

    /**
     * Persists a batch of assignments.
     *
     * @param entities rows to persist
     * @return the persisted rows
     */
    List<GroupPermission> saveAll(List<GroupPermission> entities);

    /**
     * Deletes a batch of assignments.
     *
     * @param entities rows to delete
     */
    void deleteAll(List<GroupPermission> entities);
}

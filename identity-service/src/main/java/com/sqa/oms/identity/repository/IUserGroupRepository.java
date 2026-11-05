package com.sqa.oms.identity.repository;

import com.sqa.oms.identity.entity.User;
import com.sqa.oms.identity.entity.UserGroup;

import java.util.List;

/**
 * Domain facing access to the user_group join table.
 */
public interface IUserGroupRepository {

    /**
     * Finds every group membership of a user.
     *
     * @param user owner of the memberships
     * @return the matching rows
     */
    List<UserGroup> findByUser(User user);

    /**
     * Persists a batch of memberships.
     *
     * @param entities rows to persist
     * @return the persisted rows
     */
    List<UserGroup> saveAll(List<UserGroup> entities);

    /**
     * Deletes a batch of memberships.
     *
     * @param entities rows to delete
     */
    void deleteAll(List<UserGroup> entities);
}

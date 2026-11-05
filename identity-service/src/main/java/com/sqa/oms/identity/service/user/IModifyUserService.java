package com.sqa.oms.identity.service.user;

import com.sqa.oms.identity.modules.user.model.ModifyUserRequest;

/**
 * Updates a user and its group memberships.
 */
public interface IModifyUserService {

    /**
     * Updates a user and replaces its whole set of group memberships.
     *
     * @param entityId business key of the user
     * @param request  the modify request
     */
    void update(String entityId, ModifyUserRequest request);
}

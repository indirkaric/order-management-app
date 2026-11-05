package com.sqa.oms.identity.modules.user.service;

import com.sqa.oms.identity.enums.ExpandOption;
import com.sqa.oms.identity.modules.user.model.UserResponse;

/**
 * Builds the response of the single user endpoint.
 */
public interface IUserResponseCreatorService {

    /**
     * Returns a single user, optionally expanded with its group memberships.
     *
     * @param entityId business key of the user
     * @param expand   optional response expansion
     * @return UserResponse
     */
    UserResponse create(String entityId, ExpandOption expand);
}

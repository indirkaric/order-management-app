package com.sqa.oms.identity.service.user;

import com.sqa.oms.identity.modules.user.model.CreateUserRequest;

/**
 * Creates a user and its group memberships.
 */
public interface ICreateUserService {

    /**
     * Saves a new user.
     *
     * @param request the create request
     * @return the business key of the created user
     */
    String save(CreateUserRequest request);
}

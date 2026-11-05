package com.sqa.oms.identity.modules.user.service;

import com.sqa.oms.identity.dto.response.EntityCreatedResponse;
import com.sqa.oms.identity.modules.user.model.CreateUserRequest;

/**
 * Builds the response of the user create endpoint.
 */
public interface ICreateUserResponseCreatorService {

    /**
     * Creates a user and wraps its business key in a response.
     *
     * @param request the create request
     * @return EntityCreatedResponse
     */
    EntityCreatedResponse create(CreateUserRequest request);
}

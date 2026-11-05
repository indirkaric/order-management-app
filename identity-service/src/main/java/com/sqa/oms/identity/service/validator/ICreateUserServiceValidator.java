package com.sqa.oms.identity.service.validator;

import com.sqa.oms.identity.modules.user.model.CreateUserRequest;

/**
 * Validates the user create flow.
 */
public interface ICreateUserServiceValidator {

    /**
     * Validates a create user request.
     *
     * @param request request to validate
     */
    void validate(CreateUserRequest request);
}

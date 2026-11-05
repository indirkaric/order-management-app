package com.sqa.oms.identity.service.validator;

import com.sqa.oms.identity.entity.User;
import com.sqa.oms.identity.modules.user.model.ModifyUserRequest;

/**
 * Validates the user update flow.
 */
public interface IModifyUserServiceValidator {

    /**
     * Validates an update user request against the user being modified.
     *
     * @param user    user being modified
     * @param request request to validate
     */
    void validate(User user, ModifyUserRequest request);
}

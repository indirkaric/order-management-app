package com.sqa.oms.identity.service.validator;

import com.sqa.oms.identity.modules.group.model.CreateGroupRequest;

/**
 * Validates the group create flow.
 */
public interface ICreateGroupServiceValidator {

    /**
     * Validates a create group request.
     *
     * @param request request to validate
     */
    void validate(CreateGroupRequest request);
}

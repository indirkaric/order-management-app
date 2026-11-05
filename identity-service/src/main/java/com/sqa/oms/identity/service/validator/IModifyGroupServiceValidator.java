package com.sqa.oms.identity.service.validator;

import com.sqa.oms.identity.entity.Group;
import com.sqa.oms.identity.modules.group.model.UpdateGroupRequest;

/**
 * Validates the group update flow.
 */
public interface IModifyGroupServiceValidator {

    /**
     * Validates an update group request against the group being modified.
     *
     * @param group   group being modified
     * @param request request to validate
     */
    void validate(Group group, UpdateGroupRequest request);
}

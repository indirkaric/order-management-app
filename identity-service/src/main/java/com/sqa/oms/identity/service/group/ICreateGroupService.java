package com.sqa.oms.identity.service.group;

import com.sqa.oms.identity.modules.group.model.CreateGroupRequest;

/**
 * Creates a group and its permission assignments.
 */
public interface ICreateGroupService {

    /**
     * Saves a new group.
     *
     * @param request the create request
     * @return the business key of the created group
     */
    String save(CreateGroupRequest request);
}

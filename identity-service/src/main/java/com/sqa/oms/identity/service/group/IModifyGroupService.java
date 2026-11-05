package com.sqa.oms.identity.service.group;

import com.sqa.oms.identity.modules.group.model.UpdateGroupRequest;

/**
 * Updates a group and its permission assignments.
 */
public interface IModifyGroupService {

    /**
     * Updates a group and replaces its whole set of permission assignments.
     *
     * @param entityId business key of the group
     * @param request  the update request
     */
    void update(String entityId, UpdateGroupRequest request);
}

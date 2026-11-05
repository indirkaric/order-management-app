package com.sqa.oms.identity.modules.group.service;

import com.sqa.oms.identity.enums.ExpandOption;
import com.sqa.oms.identity.modules.group.model.GroupResponse;

/**
 * Builds the response of the single group endpoint.
 */
public interface IGroupResponseCreatorService {

    /**
     * Returns a single group, optionally expanded with its permissions.
     *
     * @param entityId business key of the group
     * @param expand   optional response expansion
     * @return GroupResponse
     */
    GroupResponse create(String entityId, ExpandOption expand);
}

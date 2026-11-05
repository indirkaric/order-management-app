package com.sqa.oms.ui.service.identity;

import com.sqa.oms.ui.modules.identity.model.CreateGroupRequest;
import com.sqa.oms.ui.modules.identity.model.EntityCreatedResponse;
import com.sqa.oms.ui.modules.identity.model.ExpandOption;
import com.sqa.oms.ui.modules.identity.model.GroupListRequest;
import com.sqa.oms.ui.modules.identity.model.GroupListResponse;
import com.sqa.oms.ui.modules.identity.model.GroupResponse;
import com.sqa.oms.ui.modules.identity.model.UpdateGroupRequest;
import org.springframework.data.domain.Pageable;

/**
 * Proxy to the group resources of the identity service.
 */
public interface IGroupService {

    /**
     * Retrieves the group list.
     *
     * @param request  list filters
     * @param pageable paging information
     * @return group list
     */
    GroupListResponse getAll(GroupListRequest request, Pageable pageable);

    /**
     * Retrieves a single group.
     *
     * @param groupId group identifier
     * @param expand  optional response expansion
     * @return group
     */
    GroupResponse getById(String groupId, ExpandOption expand);

    /**
     * Creates a group.
     *
     * @param request request body
     * @return the business key of the created group
     */
    EntityCreatedResponse create(CreateGroupRequest request);

    /**
     * Updates a group and replaces its whole set of permission assignments.
     *
     * @param groupId group identifier
     * @param request request body
     */
    void update(String groupId, UpdateGroupRequest request);

    /**
     * Deletes a group.
     *
     * @param groupId group identifier
     */
    void delete(String groupId);
}

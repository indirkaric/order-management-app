package com.sqa.oms.identity.modules.group.service;

import com.sqa.oms.identity.modules.group.model.GroupListRequest;
import com.sqa.oms.identity.modules.group.model.GroupListResponse;
import org.springframework.data.domain.Pageable;

/**
 * Builds the response of the group list endpoint.
 */
public interface IGroupListResponseCreatorService {

    /**
     * Returns the filtered and paged list of groups.
     *
     * @param request  list filters
     * @param pageable paging and sorting
     * @return GroupListResponse
     */
    GroupListResponse create(GroupListRequest request, Pageable pageable);
}

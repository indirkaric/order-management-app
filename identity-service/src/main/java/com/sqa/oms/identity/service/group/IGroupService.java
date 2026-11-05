package com.sqa.oms.identity.service.group;

import com.sqa.oms.identity.dto.GroupDTO;
import com.sqa.oms.identity.modules.group.model.GroupListRequest;
import com.sqa.oms.identity.service.model.PagedList;
import org.springframework.data.domain.Pageable;

/**
 * Read access to groups.
 */
public interface IGroupService {

    /**
     * Returns a single group.
     *
     * @param entityId business key of the group
     * @return the matching group
     */
    GroupDTO getByEntityId(String entityId);

    /**
     * Returns the filtered and paged list of groups.
     *
     * @param request  list filters
     * @param pageable paging and sorting
     * @return the matching page of groups
     */
    PagedList<GroupDTO> getPagedList(GroupListRequest request, Pageable pageable);
}

package com.sqa.oms.identity.service.user;

import com.sqa.oms.identity.dto.UserDTO;
import com.sqa.oms.identity.modules.user.model.UserListRequest;
import com.sqa.oms.identity.service.model.PagedList;
import org.springframework.data.domain.Pageable;

/**
 * Read access to users.
 */
public interface IUserService {

    /**
     * Returns a single user.
     *
     * @param entityId business key of the user
     * @return the matching user
     */
    UserDTO getByEntityId(String entityId);

    /**
     * Returns the filtered and paged list of users.
     *
     * @param request  list filters
     * @param pageable paging and sorting
     * @return the matching page of users
     */
    PagedList<UserDTO> getPagedList(UserListRequest request, Pageable pageable);
}

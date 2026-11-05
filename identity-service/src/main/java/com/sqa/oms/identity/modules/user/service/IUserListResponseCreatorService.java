package com.sqa.oms.identity.modules.user.service;

import com.sqa.oms.identity.modules.user.model.UserListRequest;
import com.sqa.oms.identity.modules.user.model.UserListResponse;
import org.springframework.data.domain.Pageable;

/**
 * Builds the response of the user list endpoint.
 */
public interface IUserListResponseCreatorService {

    /**
     * Returns the filtered and paged list of users.
     *
     * @param request  list filters
     * @param pageable paging and sorting
     * @return UserListResponse
     */
    UserListResponse create(UserListRequest request, Pageable pageable);
}

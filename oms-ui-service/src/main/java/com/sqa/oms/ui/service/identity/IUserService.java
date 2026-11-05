package com.sqa.oms.ui.service.identity;

import com.sqa.oms.ui.modules.identity.model.CreateUserRequest;
import com.sqa.oms.ui.modules.identity.model.EntityCreatedResponse;
import com.sqa.oms.ui.modules.identity.model.ExpandOption;
import com.sqa.oms.ui.modules.identity.model.ModifyUserRequest;
import com.sqa.oms.ui.modules.identity.model.UserListRequest;
import com.sqa.oms.ui.modules.identity.model.UserListResponse;
import com.sqa.oms.ui.modules.identity.model.UserResponse;
import org.springframework.data.domain.Pageable;

/**
 * Proxy to the user resources of the identity service.
 */
public interface IUserService {

    /**
     * Retrieves the user list.
     *
     * @param request  list filters
     * @param pageable paging information
     * @return user list
     */
    UserListResponse getAll(UserListRequest request, Pageable pageable);

    /**
     * Retrieves a single user.
     *
     * @param userId user identifier
     * @param expand optional response expansion
     * @return user
     */
    UserResponse getById(String userId, ExpandOption expand);

    /**
     * Creates a user.
     *
     * @param request request body
     * @return the business key of the created user
     */
    EntityCreatedResponse create(CreateUserRequest request);

    /**
     * Updates a user and replaces its whole set of group memberships.
     *
     * @param userId  user identifier
     * @param request request body
     */
    void update(String userId, ModifyUserRequest request);

    /**
     * Deletes a user.
     *
     * @param userId user identifier
     */
    void delete(String userId);
}

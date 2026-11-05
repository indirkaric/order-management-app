package com.sqa.oms.identity.repository;

import com.sqa.oms.identity.entity.User;
import com.sqa.oms.identity.modules.user.model.UserListRequest;
import com.sqa.oms.identity.repository.projection.UserProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Domain facing access to the user table.
 */
public interface IUserRepository {

    /**
     * Persists a user.
     *
     * @param entity user to persist
     * @return the persisted user
     */
    User save(User entity);

    /**
     * Finds a user by its business key.
     *
     * @param entityId business key
     * @return the matching user
     */
    Optional<User> findByEntityId(String entityId);

    /**
     * Finds the projection of a user by its business key.
     *
     * @param entityId business key
     * @return the matching user projection
     */
    Optional<UserProjection> findProjectionByEntityId(String entityId);

    /**
     * Checks whether an email is already taken.
     *
     * @param email email to look up
     * @return true when the email is taken
     */
    boolean existsByEmail(String email);

    /**
     * Checks whether a business key is already taken.
     *
     * @param entityId business key
     * @return true when the key is taken
     */
    boolean existsByEntityId(String entityId);

    /**
     * Returns the filtered and paged list of user projections.
     *
     * @param request  list filters
     * @param pageable paging and sorting
     * @return the matching page of user projections
     */
    Page<UserProjection> findPagedList(UserListRequest request, Pageable pageable);
}

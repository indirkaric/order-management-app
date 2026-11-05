package com.sqa.oms.identity.repository;

import com.sqa.oms.identity.entity.Group;
import com.sqa.oms.identity.modules.group.model.GroupListRequest;
import com.sqa.oms.identity.repository.projection.GroupProjection;
import com.sqa.oms.identity.repository.projection.GroupSummaryProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Domain facing access to the group table.
 */
public interface IGroupRepository {

    /**
     * Persists a group.
     *
     * @param entity group to persist
     * @return the persisted group
     */
    Group save(Group entity);

    /**
     * Finds a group by its business key.
     *
     * @param entityId business key
     * @return the matching group
     */
    Optional<Group> findByEntityId(String entityId);

    /**
     * Finds the projection of a group by its business key.
     *
     * @param entityId business key
     * @return the matching group projection
     */
    Optional<GroupProjection> findProjectionByEntityId(String entityId);

    /**
     * Finds all groups matching the given business keys.
     *
     * @param entityIds business keys
     * @return the matching groups
     */
    List<Group> findAllByEntityIdIn(List<String> entityIds);

    /**
     * Checks whether a group name is already taken.
     *
     * @param name group name
     * @return true when the name is taken
     */
    boolean existsByName(String name);

    /**
     * Checks whether a business key is already taken.
     *
     * @param entityId business key
     * @return true when the key is taken
     */
    boolean existsByEntityId(String entityId);

    /**
     * Checks whether any user is still a member of the group.
     *
     * @param entityId business key of the group
     * @return true when at least one user is a member
     */
    boolean hasGroupUsers(String entityId);

    /**
     * Returns the filtered and paged list of group projections.
     *
     * @param request  list filters
     * @param pageable paging and sorting
     * @return the matching page of group projections
     */
    Page<GroupProjection> findPagedList(GroupListRequest request, Pageable pageable);

    /**
     * Returns the short projections of every group a user belongs to.
     *
     * @param userEntityId business key of the user
     * @return the matching group summary projections
     */
    List<GroupSummaryProjection> findSummaryProjectionsByUserEntityId(String userEntityId);
}

package com.sqa.oms.identity.repository.impl;

import com.sqa.oms.identity.entity.Group;
import com.sqa.oms.identity.entity.Group_;
import com.sqa.oms.identity.entity.User_;
import com.sqa.oms.identity.entity.UserGroup;
import com.sqa.oms.identity.entity.UserGroup_;
import com.sqa.oms.identity.entity.id.UserGroupId_;
import com.sqa.oms.identity.modules.group.model.GroupListRequest;
import com.sqa.oms.identity.repository.IGroupRepository;
import com.sqa.oms.identity.repository.projection.GroupProjection;
import com.sqa.oms.identity.repository.projection.GroupSummaryProjection;
import com.sqa.oms.identity.util.Check;
import com.sqa.oms.identity.util.CollectionHelper;
import com.sqa.oms.identity.util.SortHelper;
import com.sqa.oms.identity.util.StringHelper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Criteria API based access to the group table.
 */
@Repository
public class GroupRepository implements IGroupRepository {

    // Constructor injection: EntityManager is still a container-managed persistence context proxy,
    // but it is a regular injectable bean for this service's single persistence unit, so it is wired
    // through the constructor like every other dependency, matching the artifact/identity convention.
    @PersistenceContext
    private final EntityManager entityManager;

    /**
     * Constructor with dependency injection.
     *
     * @param entityManager the persistence context of this service
     */
    public GroupRepository(final EntityManager entityManager) {
        Check.isNotNull(entityManager, "entityManager");
        this.entityManager = entityManager;
    }

    @Override
    public Group save(final Group entity) {
        Check.isNotNull(entity, "entity");

        Group result = entity;
        if (entity.getId() == null) {
            entityManager.persist(entity);
        } else {
            result = entityManager.merge(entity);
        }

        entityManager.flush();

        return result;
    }

    @Override
    public Optional<Group> findByEntityId(final String entityId) {
        Check.isNotEmpty(entityId, "entityId");

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Group> criteriaQuery = criteriaBuilder.createQuery(Group.class);
        Root<Group> root = criteriaQuery.from(Group.class);

        criteriaQuery.where(criteriaBuilder.equal(root.get(Group_.entityId), entityId));

        return entityManager.createQuery(criteriaQuery).getResultList().stream().findFirst();
    }

    @Override
    public Optional<GroupProjection> findProjectionByEntityId(final String entityId) {
        Check.isNotEmpty(entityId, "entityId");

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<GroupProjection> criteriaQuery = criteriaBuilder.createQuery(GroupProjection.class);
        Root<Group> root = criteriaQuery.from(Group.class);

        criteriaQuery
                .multiselect(root.get(Group_.entityId), root.get(Group_.name),
                        root.get(Group_.description), root.get(Group_.status))
                .where(criteriaBuilder.equal(root.get(Group_.entityId), entityId));

        return entityManager.createQuery(criteriaQuery).getResultList().stream().findFirst();
    }

    @Override
    public List<Group> findAllByEntityIdIn(final List<String> entityIds) {
        Check.isNotNull(entityIds, "entityIds");
        if (entityIds.isEmpty()) {
            return List.of();
        }

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Group> criteriaQuery = criteriaBuilder.createQuery(Group.class);
        Root<Group> root = criteriaQuery.from(Group.class);

        criteriaQuery.where(root.get(Group_.entityId).in(entityIds));

        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    @Override
    public boolean existsByName(final String name) {
        Check.isNotEmpty(name, "name");

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<Group> root = criteriaQuery.from(Group.class);

        criteriaQuery.select(criteriaBuilder.count(root))
                .where(criteriaBuilder.equal(criteriaBuilder.lower(root.get(Group_.name)),
                        name.toLowerCase(Locale.ROOT)));

        return entityManager.createQuery(criteriaQuery).getSingleResult() > 0;
    }

    @Override
    public boolean existsByEntityId(final String entityId) {
        Check.isNotEmpty(entityId, "entityId");

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<Group> root = criteriaQuery.from(Group.class);

        criteriaQuery.select(criteriaBuilder.count(root))
                .where(criteriaBuilder.equal(root.get(Group_.entityId), entityId));

        return entityManager.createQuery(criteriaQuery).getSingleResult() > 0;
    }

    @Override
    public boolean hasGroupUsers(final String entityId) {
        Check.isNotEmpty(entityId, "entityId");

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<UserGroup> root = criteriaQuery.from(UserGroup.class);

        criteriaQuery.select(criteriaBuilder.count(root))
                .where(criteriaBuilder.equal(
                        root.get(UserGroup_.userGroupId).get(UserGroupId_.group).get(Group_.entityId), entityId));

        return entityManager.createQuery(criteriaQuery).getSingleResult() > 0;
    }

    @Override
    public Page<GroupProjection> findPagedList(final GroupListRequest request, final Pageable pageable) {
        Check.isNotNull(request, "request");
        Check.isNotNull(pageable, "pageable");

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<GroupProjection> criteriaQuery = criteriaBuilder.createQuery(GroupProjection.class);
        Root<Group> root = criteriaQuery.from(Group.class);

        List<Predicate> predicates = preparePredicates(request, root, criteriaBuilder);

        Order ordering = SortHelper.getOrdering(criteriaBuilder, root, pageable);
        if (ordering != null) {
            criteriaQuery.orderBy(ordering);
        }

        criteriaQuery
                .multiselect(root.get(Group_.entityId), root.get(Group_.name),
                        root.get(Group_.description), root.get(Group_.status))
                .where(predicates.toArray(new Predicate[0]));

        long totalNumberOfElements = prepareCountQuery(request);
        List<GroupProjection> resultList = new ArrayList<>();
        if (totalNumberOfElements > 0) {
            resultList = entityManager.createQuery(criteriaQuery)
                    .setFirstResult((int) pageable.getOffset())
                    .setMaxResults(pageable.getPageSize())
                    .getResultList();
        }

        return new PageImpl<>(resultList, pageable, totalNumberOfElements);
    }

    @Override
    public List<GroupSummaryProjection> findSummaryProjectionsByUserEntityId(final String userEntityId) {
        Check.isNotEmpty(userEntityId, "userEntityId");

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<GroupSummaryProjection> criteriaQuery = criteriaBuilder
                .createQuery(GroupSummaryProjection.class);
        Root<UserGroup> root = criteriaQuery.from(UserGroup.class);

        criteriaQuery
                .multiselect(root.get(UserGroup_.userGroupId).get(UserGroupId_.group).get(Group_.entityId),
                        root.get(UserGroup_.userGroupId).get(UserGroupId_.group).get(Group_.name))
                .where(criteriaBuilder.equal(
                        root.get(UserGroup_.userGroupId).get(UserGroupId_.user).get(User_.entityId), userEntityId));

        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    private List<Predicate> preparePredicates(final GroupListRequest request, final Root<Group> root,
                                              final CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        if (CollectionHelper.isNotEmpty(request.getIds())) {
            predicates.add(root.get(Group_.entityId).in(request.getIds()));
        }

        if (request.getStatus() != null) {
            predicates.add(criteriaBuilder.equal(root.get(Group_.status), request.getStatus()));
        }

        if (!StringHelper.isEmpty(request.getTextSearch())) {
            String pattern = "%" + request.getTextSearch().trim().toLowerCase(Locale.ROOT) + "%";
            predicates.add(criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get(Group_.name)), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get(Group_.description)), pattern)));
        }

        return predicates;
    }

    private long prepareCountQuery(final GroupListRequest request) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<Group> countRoot = countQuery.from(Group.class);

        List<Predicate> predicates = preparePredicates(request, countRoot, criteriaBuilder);
        countQuery.select(criteriaBuilder.count(countRoot)).where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(countQuery).getSingleResult();
    }
}

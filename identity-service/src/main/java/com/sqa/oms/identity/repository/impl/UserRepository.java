package com.sqa.oms.identity.repository.impl;

import com.sqa.oms.identity.entity.Group_;
import com.sqa.oms.identity.entity.User;
import com.sqa.oms.identity.entity.UserGroup;
import com.sqa.oms.identity.entity.UserGroup_;
import com.sqa.oms.identity.entity.User_;
import com.sqa.oms.identity.entity.id.UserGroupId_;
import com.sqa.oms.identity.modules.user.model.UserListRequest;
import com.sqa.oms.identity.repository.IUserRepository;
import com.sqa.oms.identity.repository.projection.UserProjection;
import com.sqa.oms.identity.util.Check;
import com.sqa.oms.identity.util.CollectionHelper;
import com.sqa.oms.identity.util.SortHelper;
import com.sqa.oms.identity.util.StringHelper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
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
 * Criteria API based access to the user table.
 */
@Repository
public final class UserRepository implements IUserRepository {

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
    public UserRepository(final EntityManager entityManager) {
        Check.isNotNull(entityManager, "entityManager");
        this.entityManager = entityManager;
    }

    @Override
    public User save(final User entity) {
        Check.isNotNull(entity, "entity");

        User result = entity;
        if (entity.getId() == null) {
            entityManager.persist(entity);
        } else {
            result = entityManager.merge(entity);
        }

        entityManager.flush();

        return result;
    }

    @Override
    public Optional<User> findByEntityId(final String entityId) {
        Check.isNotEmpty(entityId, "entityId");

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);
        Root<User> root = criteriaQuery.from(User.class);

        criteriaQuery.where(criteriaBuilder.equal(root.get(User_.entityId), entityId));

        return entityManager.createQuery(criteriaQuery).getResultList().stream().findFirst();
    }

    @Override
    public Optional<UserProjection> findProjectionByEntityId(final String entityId) {
        Check.isNotEmpty(entityId, "entityId");

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserProjection> criteriaQuery = criteriaBuilder.createQuery(UserProjection.class);
        Root<User> root = criteriaQuery.from(User.class);

        criteriaQuery
                .multiselect(root.get(User_.entityId), root.get(User_.firstName),
                        root.get(User_.lastName), root.get(User_.email), root.get(User_.status))
                .where(criteriaBuilder.equal(root.get(User_.entityId), entityId));

        return entityManager.createQuery(criteriaQuery).getResultList().stream().findFirst();
    }

    @Override
    public boolean existsByEmail(final String email) {
        Check.isNotEmpty(email, "email");

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<User> root = criteriaQuery.from(User.class);

        criteriaQuery.select(criteriaBuilder.count(root))
                .where(criteriaBuilder.equal(criteriaBuilder.lower(root.get(User_.email)),
                        email.toLowerCase(Locale.ROOT)));

        return entityManager.createQuery(criteriaQuery).getSingleResult() > 0;
    }

    @Override
    public boolean existsByEntityId(final String entityId) {
        Check.isNotEmpty(entityId, "entityId");

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<User> root = criteriaQuery.from(User.class);

        criteriaQuery.select(criteriaBuilder.count(root))
                .where(criteriaBuilder.equal(root.get(User_.entityId), entityId));

        return entityManager.createQuery(criteriaQuery).getSingleResult() > 0;
    }

    @Override
    public Page<UserProjection> findPagedList(final UserListRequest request, final Pageable pageable) {
        Check.isNotNull(request, "request");
        Check.isNotNull(pageable, "pageable");

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserProjection> criteriaQuery = criteriaBuilder.createQuery(UserProjection.class);
        Root<User> root = criteriaQuery.from(User.class);

        List<Predicate> predicates = preparePredicates(request, root, criteriaBuilder);

        Order ordering = SortHelper.getOrdering(criteriaBuilder, root, pageable);
        if (ordering != null) {
            criteriaQuery.orderBy(ordering);
        }

        criteriaQuery
                .multiselect(root.get(User_.entityId), root.get(User_.firstName),
                        root.get(User_.lastName), root.get(User_.email), root.get(User_.status))
                .distinct(true)
                .where(predicates.toArray(new Predicate[0]));

        long totalNumberOfElements = prepareCountQuery(request);
        List<UserProjection> resultList = new ArrayList<>();
        if (totalNumberOfElements > 0) {
            resultList = entityManager.createQuery(criteriaQuery)
                    .setFirstResult((int) pageable.getOffset())
                    .setMaxResults(pageable.getPageSize())
                    .getResultList();
        }

        return new PageImpl<>(resultList, pageable, totalNumberOfElements);
    }

    private List<Predicate> preparePredicates(final UserListRequest request, final Root<User> root,
                                              final CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        if (CollectionHelper.isNotEmpty(request.getIds())) {
            predicates.add(root.get(User_.entityId).in(request.getIds()));
        }

        if (!StringHelper.isEmpty(request.getEmail())) {
            predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get(User_.email)),
                    request.getEmail().trim().toLowerCase(Locale.ROOT)));
        }

        if (request.getStatus() != null) {
            predicates.add(criteriaBuilder.equal(root.get(User_.status), request.getStatus()));
        }

        if (!StringHelper.isEmpty(request.getTextSearch())) {
            String pattern = "%" + request.getTextSearch().trim().toLowerCase(Locale.ROOT) + "%";
            Expression<String> fullName = criteriaBuilder.lower(criteriaBuilder.concat(
                    criteriaBuilder.concat(root.get(User_.firstName), " "), root.get(User_.lastName)));
            predicates.add(criteriaBuilder.or(
                    criteriaBuilder.like(fullName, pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get(User_.email)), pattern)));
        }

        if (CollectionHelper.isNotEmpty(request.getGroupIds())) {
            Join<User, UserGroup> membershipJoin = root.join(User_.listOfUserGroup, JoinType.INNER);
            predicates.add(membershipJoin.get(UserGroup_.userGroupId).get(UserGroupId_.group)
                    .get(Group_.entityId).in(request.getGroupIds()));
        }

        return predicates;
    }

    private long prepareCountQuery(final UserListRequest request) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<User> countRoot = countQuery.from(User.class);

        List<Predicate> predicates = preparePredicates(request, countRoot, criteriaBuilder);
        countQuery.select(criteriaBuilder.countDistinct(countRoot)).where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(countQuery).getSingleResult();
    }
}

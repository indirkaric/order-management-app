package com.sqa.oms.identity.repository.impl;

import com.sqa.oms.identity.entity.Group_;
import com.sqa.oms.identity.entity.GroupPermission;
import com.sqa.oms.identity.entity.GroupPermission_;
import com.sqa.oms.identity.entity.Permission;
import com.sqa.oms.identity.entity.Permission_;
import com.sqa.oms.identity.entity.id.GroupPermissionId_;
import com.sqa.oms.identity.modules.permission.model.PermissionListRequest;
import com.sqa.oms.identity.repository.IPermissionRepository;
import com.sqa.oms.identity.repository.projection.PermissionProjection;
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
 * Criteria API based access to the permission table.
 */
@Repository
public class PermissionRepository implements IPermissionRepository {

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
    public PermissionRepository(final EntityManager entityManager) {
        Check.isNotNull(entityManager, "entityManager");
        this.entityManager = entityManager;
    }

    @Override
    public Optional<PermissionProjection> findProjectionByEntityId(final String entityId) {
        Check.isNotEmpty(entityId, "entityId");

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<PermissionProjection> criteriaQuery = criteriaBuilder.createQuery(PermissionProjection.class);
        Root<Permission> root = criteriaQuery.from(Permission.class);

        criteriaQuery
                .multiselect(root.get(Permission_.entityId), root.get(Permission_.name),
                        root.get(Permission_.description), root.get(Permission_.status))
                .where(criteriaBuilder.equal(root.get(Permission_.entityId), entityId));

        return entityManager.createQuery(criteriaQuery).getResultList().stream().findFirst();
    }

    @Override
    public List<Permission> findAllByEntityIdIn(final List<String> entityIds) {
        Check.isNotNull(entityIds, "entityIds");
        if (entityIds.isEmpty()) {
            return List.of();
        }

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Permission> criteriaQuery = criteriaBuilder.createQuery(Permission.class);
        Root<Permission> root = criteriaQuery.from(Permission.class);

        criteriaQuery.where(root.get(Permission_.entityId).in(entityIds));

        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    @Override
    public Page<PermissionProjection> findPagedList(final PermissionListRequest request, final Pageable pageable) {
        Check.isNotNull(request, "request");
        Check.isNotNull(pageable, "pageable");

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<PermissionProjection> criteriaQuery = criteriaBuilder.createQuery(PermissionProjection.class);
        Root<Permission> root = criteriaQuery.from(Permission.class);

        List<Predicate> predicates = preparePredicates(request, root, criteriaBuilder);

        Order ordering = SortHelper.getOrdering(criteriaBuilder, root, pageable);
        if (ordering != null) {
            criteriaQuery.orderBy(ordering);
        }

        criteriaQuery
                .multiselect(root.get(Permission_.entityId), root.get(Permission_.name),
                        root.get(Permission_.description), root.get(Permission_.status))
                .where(predicates.toArray(new Predicate[0]));

        long totalNumberOfElements = prepareCountQuery(request);
        List<PermissionProjection> resultList = new ArrayList<>();
        if (totalNumberOfElements > 0) {
            resultList = entityManager.createQuery(criteriaQuery)
                    .setFirstResult((int) pageable.getOffset())
                    .setMaxResults(pageable.getPageSize())
                    .getResultList();
        }

        return new PageImpl<>(resultList, pageable, totalNumberOfElements);
    }

    @Override
    public List<String> findNamesByGroupEntityId(final String groupEntityId) {
        Check.isNotEmpty(groupEntityId, "groupEntityId");

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<String> criteriaQuery = criteriaBuilder.createQuery(String.class);
        Root<GroupPermission> root = criteriaQuery.from(GroupPermission.class);

        criteriaQuery
                .select(root.get(GroupPermission_.groupPermissionId).get(GroupPermissionId_.permission)
                        .get(Permission_.name))
                .where(criteriaBuilder.equal(
                        root.get(GroupPermission_.groupPermissionId).get(GroupPermissionId_.group)
                                .get(Group_.entityId),
                        groupEntityId));

        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    private List<Predicate> preparePredicates(final PermissionListRequest request, final Root<Permission> root,
                                              final CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        if (CollectionHelper.isNotEmpty(request.getIds())) {
            predicates.add(root.get(Permission_.entityId).in(request.getIds()));
        }

        if (request.getStatus() != null) {
            predicates.add(criteriaBuilder.equal(root.get(Permission_.status), request.getStatus()));
        }

        if (!StringHelper.isEmpty(request.getTextSearch())) {
            String pattern = "%" + request.getTextSearch().trim().toLowerCase(Locale.ROOT) + "%";
            predicates.add(criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get(Permission_.name)), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get(Permission_.description)), pattern)));
        }

        return predicates;
    }

    private long prepareCountQuery(final PermissionListRequest request) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<Permission> countRoot = countQuery.from(Permission.class);

        List<Predicate> predicates = preparePredicates(request, countRoot, criteriaBuilder);
        countQuery.select(criteriaBuilder.count(countRoot)).where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(countQuery).getSingleResult();
    }
}

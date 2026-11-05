package com.sqa.oms.identity.repository.impl;

import com.sqa.oms.identity.entity.Group;
import com.sqa.oms.identity.entity.GroupPermission;
import com.sqa.oms.identity.entity.GroupPermission_;
import com.sqa.oms.identity.entity.id.GroupPermissionId_;
import com.sqa.oms.identity.repository.IGroupPermissionRepository;
import com.sqa.oms.identity.util.Check;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Criteria API based access to the group_permission join table.
 */
@Repository
public class GroupPermissionRepository implements IGroupPermissionRepository {

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
    public GroupPermissionRepository(final EntityManager entityManager) {
        Check.isNotNull(entityManager, "entityManager");
        this.entityManager = entityManager;
    }

    @Override
    public List<GroupPermission> findByGroup(final Group group) {
        Check.isNotNull(group, "group");

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<GroupPermission> criteriaQuery = criteriaBuilder.createQuery(GroupPermission.class);
        Root<GroupPermission> root = criteriaQuery.from(GroupPermission.class);

        criteriaQuery.where(criteriaBuilder.equal(
                root.get(GroupPermission_.groupPermissionId).get(GroupPermissionId_.group), group));

        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    @Override
    public List<GroupPermission> saveAll(final List<GroupPermission> entities) {
        Check.isNotNull(entities, "entities");

        entities.forEach(entityManager::persist);
        entityManager.flush();

        return entities;
    }

    @Override
    public void deleteAll(final List<GroupPermission> entities) {
        Check.isNotNull(entities, "entities");

        entities.forEach(entityManager::remove);
        entityManager.flush();
    }
}

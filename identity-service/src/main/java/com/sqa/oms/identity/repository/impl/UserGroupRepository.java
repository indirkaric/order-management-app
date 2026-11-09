package com.sqa.oms.identity.repository.impl;

import com.sqa.oms.identity.entity.User;
import com.sqa.oms.identity.entity.UserGroup;
import com.sqa.oms.identity.entity.UserGroup_;
import com.sqa.oms.identity.entity.id.UserGroupId_;
import com.sqa.oms.identity.repository.IUserGroupRepository;
import com.sqa.oms.identity.util.Check;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Criteria API based access to the user_group join table.
 */
@Repository
public final class UserGroupRepository implements IUserGroupRepository {

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
    public UserGroupRepository(final EntityManager entityManager) {
        Check.isNotNull(entityManager, "entityManager");
        this.entityManager = entityManager;
    }

    @Override
    public List<UserGroup> findByUser(final User user) {
        Check.isNotNull(user, "user");

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserGroup> criteriaQuery = criteriaBuilder.createQuery(UserGroup.class);
        Root<UserGroup> root = criteriaQuery.from(UserGroup.class);

        criteriaQuery.where(criteriaBuilder.equal(
                root.get(UserGroup_.userGroupId).get(UserGroupId_.user), user));

        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    @Override
    public List<UserGroup> saveAll(final List<UserGroup> entities) {
        Check.isNotNull(entities, "entities");

        entities.forEach(entityManager::persist);
        entityManager.flush();

        return entities;
    }

    @Override
    public void deleteAll(final List<UserGroup> entities) {
        Check.isNotNull(entities, "entities");

        entities.forEach(entityManager::remove);
        entityManager.flush();
    }
}

package com.sqa.oms.order.modules.product.repository.impl;

import com.sqa.oms.order.entity.ProductDocument;
import com.sqa.oms.order.entity.ProductDocument_;
import com.sqa.oms.order.modules.product.repository.IProductDocumentRepository;
import com.sqa.oms.order.util.Check;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Criteria API based access to the product_document table.
 */
@Repository
public class ProductDocumentRepository implements IProductDocumentRepository {

    // Field injection: EntityManager is a container-managed persistence context proxy,
    // not a plain singleton bean, so it is wired through @PersistenceContext rather than the constructor.
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public ProductDocument save(final ProductDocument document) {
        Check.isNotNull(document, "document");

        ProductDocument result = document;
        if (document.getId() == null) {
            entityManager.persist(document);
        } else {
            result = entityManager.merge(document);
        }

        entityManager.flush();

        return result;
    }

    @Override
    public List<ProductDocument> findByProductId(final Long productId) {
        Check.isNotNull(productId, "productId");

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ProductDocument> criteriaQuery = criteriaBuilder.createQuery(ProductDocument.class);
        Root<ProductDocument> root = criteriaQuery.from(ProductDocument.class);

        Order oldestFirst = criteriaBuilder.asc(root.get(ProductDocument_.uploadedAt));
        criteriaQuery.where(criteriaBuilder.equal(root.get(ProductDocument_.product).get("id"), productId))
                .orderBy(oldestFirst);

        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    @Override
    public Optional<ProductDocument> findByEntityIdAndProductId(final String entityId, final Long productId) {
        Check.isNotEmpty(entityId, "entityId");
        Check.isNotNull(productId, "productId");

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ProductDocument> criteriaQuery = criteriaBuilder.createQuery(ProductDocument.class);
        Root<ProductDocument> root = criteriaQuery.from(ProductDocument.class);

        criteriaQuery.where(
                criteriaBuilder.equal(root.get(ProductDocument_.entityId), entityId),
                criteriaBuilder.equal(root.get(ProductDocument_.product).get("id"), productId));

        return entityManager.createQuery(criteriaQuery).getResultList().stream().findFirst();
    }

    @Override
    public boolean existsByEntityId(final String entityId) {
        Check.isNotEmpty(entityId, "entityId");

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<ProductDocument> root = criteriaQuery.from(ProductDocument.class);

        criteriaQuery.select(criteriaBuilder.count(root))
                .where(criteriaBuilder.equal(root.get(ProductDocument_.entityId), entityId));

        return entityManager.createQuery(criteriaQuery).getSingleResult() > 0;
    }
}

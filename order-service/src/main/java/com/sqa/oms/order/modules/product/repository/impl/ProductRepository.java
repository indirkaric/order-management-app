package com.sqa.oms.order.modules.product.repository.impl;

import com.sqa.oms.order.entity.Product;
import com.sqa.oms.order.entity.Product_;
import com.sqa.oms.order.modules.product.model.ProductListRequest;
import com.sqa.oms.order.modules.product.repository.IProductRepository;
import com.sqa.oms.order.modules.product.repository.projection.ProductProjection;
import com.sqa.oms.order.util.Check;
import com.sqa.oms.order.util.SortHelper;
import com.sqa.oms.order.util.StringHelper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
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
 * Criteria API based access to the product table.
 */
@Repository
public final class ProductRepository implements IProductRepository {

    // Field injection: EntityManager is a container-managed persistence context proxy,
    // not a plain singleton bean, so it is wired through @PersistenceContext rather than the constructor.
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Product save(final Product product) {
        Check.isNotNull(product, "product");

        Product result = product;
        if (product.getId() == null) {
            entityManager.persist(product);
        } else {
            result = entityManager.merge(product);
        }

        entityManager.flush();

        return result;
    }

    @Override
    public Optional<Product> findByEntityId(final String entityId) {
        Check.isNotEmpty(entityId, "entityId");

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> criteriaQuery = criteriaBuilder.createQuery(Product.class);
        Root<Product> root = criteriaQuery.from(Product.class);

        criteriaQuery.where(criteriaBuilder.equal(root.get(Product_.entityId), entityId));

        return entityManager.createQuery(criteriaQuery).getResultList().stream().findFirst();
    }

    @Override
    public Optional<ProductProjection> findProjectionByEntityId(final String entityId) {
        Check.isNotEmpty(entityId, "entityId");

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ProductProjection> criteriaQuery = criteriaBuilder.createQuery(ProductProjection.class);
        Root<Product> root = criteriaQuery.from(Product.class);

        criteriaQuery.multiselect(productProjectionSelections(root))
                .where(criteriaBuilder.equal(root.get(Product_.entityId), entityId));

        return entityManager.createQuery(criteriaQuery).getResultList().stream().findFirst();
    }

    @Override
    public Page<ProductProjection> findPagedList(final ProductListRequest request, final Pageable pageable) {
        Check.isNotNull(request, "request");
        Check.isNotNull(pageable, "pageable");

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ProductProjection> criteriaQuery = criteriaBuilder.createQuery(ProductProjection.class);
        Root<Product> root = criteriaQuery.from(Product.class);

        List<Predicate> predicates = preparePredicates(request, root, criteriaBuilder);

        jakarta.persistence.criteria.Order ordering = SortHelper.getOrdering(criteriaBuilder, root, pageable);
        if (ordering != null) {
            criteriaQuery.orderBy(ordering);
        }

        criteriaQuery.multiselect(productProjectionSelections(root)).where(predicates.toArray(new Predicate[0]));

        long totalNumberOfElements = prepareCountQuery(request);
        List<ProductProjection> resultList = new ArrayList<>();
        if (totalNumberOfElements > 0) {
            resultList = entityManager.createQuery(criteriaQuery)
                    .setFirstResult((int) pageable.getOffset())
                    .setMaxResults(pageable.getPageSize())
                    .getResultList();
        }

        return new PageImpl<>(resultList, pageable, totalNumberOfElements);
    }

    private List<jakarta.persistence.criteria.Selection<?>> productProjectionSelections(final Root<Product> root) {
        return List.of(root.get(Product_.entityId), root.get(Product_.name),
                root.get(Product_.sku), root.get(Product_.description), root.get(Product_.unitPrice));
    }

    @Override
    public boolean existsBySku(final String sku) {
        Check.isNotEmpty(sku, "sku");

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<Product> root = criteriaQuery.from(Product.class);

        criteriaQuery.select(criteriaBuilder.count(root))
                .where(criteriaBuilder.equal(criteriaBuilder.lower(root.get(Product_.sku)),
                        sku.toLowerCase(Locale.ROOT)));

        return entityManager.createQuery(criteriaQuery).getSingleResult() > 0;
    }

    @Override
    public boolean existsByEntityId(final String entityId) {
        Check.isNotEmpty(entityId, "entityId");

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<Product> root = criteriaQuery.from(Product.class);

        criteriaQuery.select(criteriaBuilder.count(root))
                .where(criteriaBuilder.equal(root.get(Product_.entityId), entityId));

        return entityManager.createQuery(criteriaQuery).getSingleResult() > 0;
    }

    private List<Predicate> preparePredicates(final ProductListRequest request, final Root<Product> root,
                                              final CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        if (!StringHelper.isEmpty(request.getSku())) {
            predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get(Product_.sku)),
                    request.getSku().toLowerCase(Locale.ROOT)));
        }

        if (!StringHelper.isEmpty(request.getTextSearch())) {
            String pattern = "%" + request.getTextSearch().toLowerCase(Locale.ROOT) + "%";
            predicates.add(criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get(Product_.name)), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get(Product_.description)), pattern)));
        }

        return predicates;
    }

    private long prepareCountQuery(final ProductListRequest request) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<Product> countRoot = countQuery.from(Product.class);

        List<Predicate> predicates = preparePredicates(request, countRoot, criteriaBuilder);
        countQuery.select(criteriaBuilder.count(countRoot)).where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(countQuery).getSingleResult();
    }
}

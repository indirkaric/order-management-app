package com.sqa.oms.order.modules.order.repository.impl;

import com.sqa.oms.order.entity.Order;
import com.sqa.oms.order.entity.Order_;
import com.sqa.oms.order.entity.OrderItem;
import com.sqa.oms.order.entity.OrderItem_;
import com.sqa.oms.order.entity.Product;
import com.sqa.oms.order.entity.Product_;
import com.sqa.oms.order.enums.OrderStatus;
import com.sqa.oms.order.modules.order.repository.IOrderRepository;
import com.sqa.oms.order.modules.order.repository.projection.OrderItemProjection;
import com.sqa.oms.order.modules.order.repository.projection.OrderProjection;
import com.sqa.oms.order.util.Check;
import com.sqa.oms.order.util.SortHelper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Criteria API based access to the orders table.
 */
@Repository
public class OrderRepository implements IOrderRepository {

    // Field injection: EntityManager is a container-managed persistence context proxy,
    // not a plain singleton bean, so it is wired through @PersistenceContext rather than the constructor.
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Order save(final Order order) {
        Check.isNotNull(order, "order");

        Order result = order;
        if (order.getId() == null) {
            entityManager.persist(order);
        } else {
            result = entityManager.merge(order);
        }

        entityManager.flush();

        return result;
    }

    @Override
    public Optional<Order> findByEntityId(final String entityId) {
        Check.isNotEmpty(entityId, "entityId");

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Order> criteriaQuery = criteriaBuilder.createQuery(Order.class);
        Root<Order> root = criteriaQuery.from(Order.class);

        criteriaQuery.where(criteriaBuilder.equal(root.get(Order_.entityId), entityId));

        return entityManager.createQuery(criteriaQuery).getResultList().stream().findFirst();
    }

    @Override
    public Optional<OrderProjection> findProjectionByEntityId(final String entityId) {
        Check.isNotEmpty(entityId, "entityId");

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<OrderProjection> criteriaQuery = criteriaBuilder.createQuery(OrderProjection.class);
        Root<Order> root = criteriaQuery.from(Order.class);

        criteriaQuery.multiselect(orderProjectionSelections(root))
                .where(criteriaBuilder.equal(root.get(Order_.entityId), entityId));

        return entityManager.createQuery(criteriaQuery).getResultList().stream().findFirst();
    }

    @Override
    public Page<OrderProjection> findPagedList(final String customerId, final OrderStatus status,
                                               final Pageable pageable) {
        Check.isNotNull(pageable, "pageable");

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<OrderProjection> criteriaQuery = criteriaBuilder.createQuery(OrderProjection.class);
        Root<Order> root = criteriaQuery.from(Order.class);

        List<Predicate> predicates = preparePredicates(customerId, status, root, criteriaBuilder);

        jakarta.persistence.criteria.Order ordering = SortHelper.getOrdering(criteriaBuilder, root, pageable);
        if (ordering != null) {
            criteriaQuery.orderBy(ordering);
        }

        criteriaQuery.multiselect(orderProjectionSelections(root)).where(predicates.toArray(new Predicate[0]));

        long totalNumberOfElements = prepareCountQuery(customerId, status);
        List<OrderProjection> resultList = new ArrayList<>();
        if (totalNumberOfElements > 0) {
            resultList = entityManager.createQuery(criteriaQuery)
                    .setFirstResult((int) pageable.getOffset())
                    .setMaxResults(pageable.getPageSize())
                    .getResultList();
        }

        return new PageImpl<>(resultList, pageable, totalNumberOfElements);
    }

    @Override
    public List<OrderItemProjection> findItemProjectionsByOrderEntityId(final String orderEntityId) {
        Check.isNotEmpty(orderEntityId, "orderEntityId");

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<OrderItemProjection> criteriaQuery = criteriaBuilder.createQuery(OrderItemProjection.class);
        Root<OrderItem> root = criteriaQuery.from(OrderItem.class);
        // The product reference is optional (an item may only carry a free text product name),
        // so it is left joined rather than inner joined.
        Join<OrderItem, Product> productJoin = root.join(OrderItem_.product, JoinType.LEFT);

        criteriaQuery
                .multiselect(productJoin.get(Product_.entityId),
                        root.get(OrderItem_.productName), root.get(OrderItem_.quantity),
                        root.get(OrderItem_.unitPrice))
                .where(criteriaBuilder.equal(root.get(OrderItem_.order).get(Order_.entityId), orderEntityId))
                .orderBy(criteriaBuilder.asc(root.get(OrderItem_.id)));

        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    @Override
    public boolean existsByEntityId(final String entityId) {
        Check.isNotEmpty(entityId, "entityId");

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<Order> root = criteriaQuery.from(Order.class);

        criteriaQuery.select(criteriaBuilder.count(root))
                .where(criteriaBuilder.equal(root.get(Order_.entityId), entityId));

        return entityManager.createQuery(criteriaQuery).getSingleResult() > 0;
    }

    @Override
    public void delete(final Order order) {
        Check.isNotNull(order, "order");

        entityManager.remove(order);
    }

    private List<jakarta.persistence.criteria.Selection<?>> orderProjectionSelections(final Root<Order> root) {
        return List.of(root.get(Order_.entityId), root.get(Order_.orderNumber), root.get(Order_.customerId),
                root.get(Order_.status), root.get(Order_.totalAmount), root.get(Order_.createdUtc));
    }

    // customerId and status are both genuinely optional filters, so predicates are built from
    // whichever ones are actually present instead of a derived-query method per combination.
    private List<Predicate> preparePredicates(final String customerId, final OrderStatus status,
                                              final Root<Order> root, final CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        if (customerId != null) {
            predicates.add(criteriaBuilder.equal(root.get(Order_.customerId), customerId));
        }

        if (status != null) {
            predicates.add(criteriaBuilder.equal(root.get(Order_.status), status));
        }

        return predicates;
    }

    private long prepareCountQuery(final String customerId, final OrderStatus status) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<Order> countRoot = countQuery.from(Order.class);

        List<Predicate> predicates = preparePredicates(customerId, status, countRoot, criteriaBuilder);
        countQuery.select(criteriaBuilder.count(countRoot)).where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(countQuery).getSingleResult();
    }
}

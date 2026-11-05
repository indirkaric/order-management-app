package com.sqa.oms.order.modules.product.repository;

import com.sqa.oms.order.entity.Product;
import com.sqa.oms.order.modules.product.model.ProductListRequest;
import com.sqa.oms.order.modules.product.repository.projection.ProductProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface IProductRepository {

    /**
     * Persists a product.
     *
     * @param product product to persist
     * @return the persisted product
     */
    Product save(Product product);

    /**
     * Looks a product up by its business key.
     *
     * @param entityId business key
     * @return the product when it exists
     */
    Optional<Product> findByEntityId(String entityId);

    /**
     * Looks the projection of a product up by its business key.
     *
     * @param entityId business key
     * @return the product projection when it exists
     */
    Optional<ProductProjection> findProjectionByEntityId(String entityId);

    /**
     * Returns the filtered and paged list of product projections.
     *
     * @param request  list filters
     * @param pageable paging information
     * @return page of product projections
     */
    Page<ProductProjection> findPagedList(ProductListRequest request, Pageable pageable);

    /**
     * Checks whether a SKU is already taken.
     *
     * @param sku sku to look up
     * @return true when the sku is already in use
     */
    boolean existsBySku(String sku);

    /**
     * Checks whether a business key is already taken.
     *
     * @param entityId business key
     * @return true when the key is taken
     */
    boolean existsByEntityId(String entityId);
}

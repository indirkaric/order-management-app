package com.sqa.oms.order.modules.product.service;

import com.sqa.oms.order.modules.product.model.ProductListRequest;
import com.sqa.oms.order.modules.product.model.ProductListResponse;
import org.springframework.data.domain.Pageable;

/**
 * Builds the response of the product list endpoint.
 */
public interface IProductListResponseCreatorService {

    /**
     * Returns the filtered and paged list of products.
     *
     * @param request  list filters
     * @param pageable paging information
     * @return ProductListResponse
     */
    ProductListResponse create(ProductListRequest request, Pageable pageable);
}

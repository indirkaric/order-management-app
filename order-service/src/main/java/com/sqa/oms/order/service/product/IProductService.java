package com.sqa.oms.order.service.product;

import com.sqa.oms.order.modules.product.dto.ProductDTO;
import com.sqa.oms.order.modules.product.model.ProductListRequest;
import com.sqa.oms.order.service.model.PagedList;
import org.springframework.data.domain.Pageable;

/**
 * Read access to catalog products.
 */
public interface IProductService {

    /**
     * Returns a single product.
     *
     * @param entityId business key of the product
     * @return the product
     */
    ProductDTO getByEntityId(String entityId);

    /**
     * Returns the filtered and paged list of products.
     *
     * @param request  list filters
     * @param pageable paging information
     * @return the matching paged list of products
     */
    PagedList<ProductDTO> getPagedList(ProductListRequest request, Pageable pageable);
}

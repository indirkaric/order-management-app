package com.sqa.oms.order.modules.product.service.impl;

import com.sqa.oms.order.modules.product.dto.ProductDTO;
import com.sqa.oms.order.modules.product.mapper.IProductMapper;
import com.sqa.oms.order.modules.product.model.ProductListRequest;
import com.sqa.oms.order.modules.product.model.ProductListResponse;
import com.sqa.oms.order.modules.product.model.ProductResponse;
import com.sqa.oms.order.modules.product.service.IProductListResponseCreatorService;
import com.sqa.oms.order.service.model.PagedList;
import com.sqa.oms.order.service.product.IProductService;
import com.sqa.oms.order.util.Check;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Default implementation of the product list response builder.
 */
@Service
public class ProductListResponseCreatorService implements IProductListResponseCreatorService {

    private final IProductService productService;

    private final IProductMapper productMapper;

    /**
     * Constructor with dependency injection.
     *
     * @param productService core product read access
     * @param productMapper  product dto and projection mapper
     */
    public ProductListResponseCreatorService(final IProductService productService,
                                             final IProductMapper productMapper) {
        Check.isNotNull(productService, "productService");
        Check.isNotNull(productMapper, "productMapper");

        this.productService = productService;
        this.productMapper = productMapper;
    }

    @Override
    public ProductListResponse create(final ProductListRequest request, final Pageable pageable) {
        Check.isNotNull(request, "request");
        Check.isNotNull(pageable, "pageable");

        PagedList<ProductDTO> pagedList = productService.getPagedList(request, pageable);
        List<ProductResponse> data = pagedList.getItems().stream().map(productMapper::toResponse).toList();

        return new ProductListResponse(data, pagedList.getCount());
    }
}

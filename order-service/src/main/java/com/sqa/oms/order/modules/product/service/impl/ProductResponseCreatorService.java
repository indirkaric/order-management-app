package com.sqa.oms.order.modules.product.service.impl;

import com.sqa.oms.order.modules.product.mapper.IProductMapper;
import com.sqa.oms.order.modules.product.model.ProductResponse;
import com.sqa.oms.order.modules.product.service.IProductResponseCreatorService;
import com.sqa.oms.order.service.product.IProductService;
import com.sqa.oms.order.util.Check;
import org.springframework.stereotype.Service;

/**
 * Default implementation of the single product response builder.
 */
@Service
public class ProductResponseCreatorService implements IProductResponseCreatorService {

    private final IProductService productService;

    private final IProductMapper productMapper;

    /**
     * Constructor with dependency injection.
     *
     * @param productService core product read access
     * @param productMapper  product dto and projection mapper
     */
    public ProductResponseCreatorService(final IProductService productService, final IProductMapper productMapper) {
        Check.isNotNull(productService, "productService");
        Check.isNotNull(productMapper, "productMapper");

        this.productService = productService;
        this.productMapper = productMapper;
    }

    @Override
    public ProductResponse create(final String entityId) {
        Check.isNotEmpty(entityId, "entityId");

        return productMapper.toResponse(productService.getByEntityId(entityId));
    }
}

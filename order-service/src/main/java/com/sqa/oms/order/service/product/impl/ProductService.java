package com.sqa.oms.order.service.product.impl;

import com.sqa.oms.order.exception.EntityNotFoundException;
import com.sqa.oms.order.modules.product.dto.ProductDTO;
import com.sqa.oms.order.modules.product.mapper.IProductMapper;
import com.sqa.oms.order.modules.product.model.ProductListRequest;
import com.sqa.oms.order.modules.product.repository.IProductRepository;
import com.sqa.oms.order.modules.product.repository.projection.ProductProjection;
import com.sqa.oms.order.service.model.PagedList;
import com.sqa.oms.order.service.product.IProductService;
import com.sqa.oms.order.util.Check;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Default implementation of the product read access.
 */
@Service
@Transactional(readOnly = true)
public class ProductService implements IProductService {

    private final IProductRepository productRepository;

    private final IProductMapper productMapper;

    /**
     * Constructor with dependency injection.
     *
     * @param productRepository product persistence
     * @param productMapper     product projection mapper
     */
    public ProductService(final IProductRepository productRepository, final IProductMapper productMapper) {
        Check.isNotNull(productRepository, "productRepository");
        Check.isNotNull(productMapper, "productMapper");

        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Override
    public ProductDTO getByEntityId(final String entityId) {
        Check.isNotEmpty(entityId, "entityId");

        return productRepository.findProjectionByEntityId(entityId)
                .map(productMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Product " + entityId + " was not found."));
    }

    @Override
    public PagedList<ProductDTO> getPagedList(final ProductListRequest request, final Pageable pageable) {
        Check.isNotNull(request, "request");
        Check.isNotNull(pageable, "pageable");

        Page<ProductProjection> page = productRepository.findPagedList(request, pageable);
        List<ProductDTO> items = page.getContent().stream().map(productMapper::toDto).toList();

        return new PagedList<>(items, page.getTotalElements());
    }
}

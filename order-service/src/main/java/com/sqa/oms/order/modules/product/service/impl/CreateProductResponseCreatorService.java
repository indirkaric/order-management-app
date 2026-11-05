package com.sqa.oms.order.modules.product.service.impl;

import com.sqa.oms.order.modules.product.model.CreateProductRequest;
import com.sqa.oms.order.modules.product.model.ProductResponse;
import com.sqa.oms.order.modules.product.service.ICreateProductResponseCreatorService;
import com.sqa.oms.order.modules.product.service.IProductResponseCreatorService;
import com.sqa.oms.order.service.product.ICreateProductService;
import com.sqa.oms.order.util.Check;
import org.springframework.stereotype.Service;

/**
 * Default implementation of the create product response builder.
 */
@Service
public class CreateProductResponseCreatorService implements ICreateProductResponseCreatorService {

    private final ICreateProductService createProductService;

    private final IProductResponseCreatorService productResponseCreatorService;

    /**
     * Constructor with dependency injection.
     *
     * @param createProductService          core product create flow
     * @param productResponseCreatorService builds the full response off the newly created entityId
     */
    public CreateProductResponseCreatorService(final ICreateProductService createProductService,
                                               final IProductResponseCreatorService productResponseCreatorService) {
        Check.isNotNull(createProductService, "createProductService");
        Check.isNotNull(productResponseCreatorService, "productResponseCreatorService");

        this.createProductService = createProductService;
        this.productResponseCreatorService = productResponseCreatorService;
    }

    @Override
    public ProductResponse create(final CreateProductRequest request) {
        Check.isNotNull(request, "request");

        String entityId = createProductService.save(request);

        return productResponseCreatorService.create(entityId);
    }
}

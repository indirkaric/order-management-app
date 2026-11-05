package com.sqa.oms.order.service.validator.impl;

import com.sqa.oms.order.exception.BusinessException;
import com.sqa.oms.order.modules.product.model.CreateProductRequest;
import com.sqa.oms.order.modules.product.repository.IProductRepository;
import com.sqa.oms.order.service.validator.ICreateProductServiceValidator;
import com.sqa.oms.order.util.Check;
import org.springframework.stereotype.Service;

/**
 * Default implementation of the product create validation.
 */
@Service
public class CreateProductServiceValidator implements ICreateProductServiceValidator {

    private final IProductRepository productRepository;

    /**
     * Constructor with dependency injection.
     *
     * @param productRepository access to the product table
     */
    public CreateProductServiceValidator(final IProductRepository productRepository) {
        Check.isNotNull(productRepository, "productRepository");

        this.productRepository = productRepository;
    }

    @Override
    public void validate(final CreateProductRequest request) {
        Check.isNotNull(request, "request");

        if (productRepository.existsBySku(request.sku())) {
            throw new BusinessException("A product with sku " + request.sku() + " already exists.");
        }
    }
}

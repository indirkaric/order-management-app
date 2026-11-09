package com.sqa.oms.order.service.product.impl;

import com.sqa.oms.common.events.EntityType;
import com.sqa.oms.common.events.EventAction;
import com.sqa.oms.order.entity.Product;
import com.sqa.oms.order.event.DomainChangeEvent;
import com.sqa.oms.order.modules.product.mapper.IProductMapper;
import com.sqa.oms.order.modules.product.model.CreateProductRequest;
import com.sqa.oms.order.modules.product.repository.IProductRepository;
import com.sqa.oms.order.service.product.ICreateProductService;
import com.sqa.oms.order.service.security.ICallerContext;
import com.sqa.oms.order.service.validator.ICreateProductServiceValidator;
import com.sqa.oms.order.util.Check;
import com.sqa.oms.order.util.EntityIdGenerator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of the product create flow.
 */
@Service
@Transactional
public class CreateProductService implements ICreateProductService {

    private final IProductRepository productRepository;

    private final IProductMapper productMapper;

    private final ICreateProductServiceValidator validator;

    private final ICallerContext callerContext;

    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Constructor with dependency injection.
     *
     * @param productRepository         product persistence
     * @param productMapper             entity to response mapper, used for the event payload
     * @param validator                 validator of the create request
     * @param callerContext             resolved caller identity
     * @param applicationEventPublisher local event publisher
     */
    public CreateProductService(final IProductRepository productRepository,
                                final IProductMapper productMapper,
                                final ICreateProductServiceValidator validator,
                                final ICallerContext callerContext,
                                final ApplicationEventPublisher applicationEventPublisher) {
        Check.isNotNull(productRepository, "productRepository");
        Check.isNotNull(productMapper, "productMapper");
        Check.isNotNull(validator, "validator");
        Check.isNotNull(callerContext, "callerContext");
        Check.isNotNull(applicationEventPublisher, "applicationEventPublisher");

        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.validator = validator;
        this.callerContext = callerContext;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * Creates a product from the given request.
     *
     * @param request the product to create
     * @return entityId of the created product
     */
    @Override
    public String save(final CreateProductRequest request) {
        Check.isNotNull(request, "request");

        validator.validate(request);

        Product product = new Product();
        product.setEntityId(generateProductEntityId());
        product.setName(request.name());
        product.setSku(request.sku());
        product.setDescription(request.description());
        product.setUnitPrice(request.unitPrice());

        Product saved = productRepository.save(product);

        applicationEventPublisher.publishEvent(new DomainChangeEvent(
                EntityType.PRODUCT, EventAction.CREATE, saved.getEntityId(), callerContext.getUserId(),
                productMapper.toResponse(saved)));

        return saved.getEntityId();
    }

    private String generateProductEntityId() {
        String candidate = EntityIdGenerator.generate();
        while (productRepository.existsByEntityId(candidate)) {
            candidate = EntityIdGenerator.generate();
        }

        return candidate;
    }
}

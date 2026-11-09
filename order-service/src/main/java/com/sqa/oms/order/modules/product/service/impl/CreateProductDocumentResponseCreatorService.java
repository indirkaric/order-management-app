package com.sqa.oms.order.modules.product.service.impl;

import com.sqa.oms.order.modules.product.model.ProductDocumentResponse;
import com.sqa.oms.order.modules.product.service.ICreateProductDocumentResponseCreatorService;
import com.sqa.oms.order.modules.product.service.IProductDocumentResponseCreatorService;
import com.sqa.oms.order.service.product.ICreateProductDocumentService;
import com.sqa.oms.order.util.Check;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Default implementation of the upload product document response builder.
 */
@Service
public final class CreateProductDocumentResponseCreatorService
        implements ICreateProductDocumentResponseCreatorService {

    private final ICreateProductDocumentService createProductDocumentService;

    private final IProductDocumentResponseCreatorService productDocumentResponseCreatorService;

    /**
     * Constructor with dependency injection.
     *
     * @param createProductDocumentService          core product document create flow
     * @param productDocumentResponseCreatorService builds the full response off the newly
     *                                               created entityId
     */
    public CreateProductDocumentResponseCreatorService(
            final ICreateProductDocumentService createProductDocumentService,
            final IProductDocumentResponseCreatorService productDocumentResponseCreatorService) {
        Check.isNotNull(createProductDocumentService, "createProductDocumentService");
        Check.isNotNull(productDocumentResponseCreatorService, "productDocumentResponseCreatorService");

        this.createProductDocumentService = createProductDocumentService;
        this.productDocumentResponseCreatorService = productDocumentResponseCreatorService;
    }

    @Override
    public ProductDocumentResponse create(final String productEntityId, final MultipartFile file) {
        Check.isNotEmpty(productEntityId, "productEntityId");

        String documentEntityId = createProductDocumentService.upload(productEntityId, file);

        return productDocumentResponseCreatorService.create(productEntityId, documentEntityId);
    }
}

package com.sqa.oms.order.modules.product.service.impl;

import com.sqa.oms.order.client.storage.StorageClient;
import com.sqa.oms.order.modules.product.dto.ProductDocumentDTO;
import com.sqa.oms.order.modules.product.mapper.IProductMapper;
import com.sqa.oms.order.modules.product.model.ProductDocumentResponse;
import com.sqa.oms.order.modules.product.service.IProductDocumentResponseCreatorService;
import com.sqa.oms.order.service.product.IProductDocumentService;
import com.sqa.oms.order.util.Check;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Default implementation of the single and list product document response builder.
 */
@Service
public final class ProductDocumentResponseCreatorService implements IProductDocumentResponseCreatorService {

    private final IProductDocumentService productDocumentService;

    private final StorageClient storageClient;

    private final IProductMapper productMapper;

    /**
     * Constructor with dependency injection.
     *
     * @param productDocumentService core product document read access
     * @param storageClient          client of the dedicated file storage service
     * @param productMapper          document dto mapper
     */
    public ProductDocumentResponseCreatorService(final IProductDocumentService productDocumentService,
                                                  final StorageClient storageClient,
                                                  final IProductMapper productMapper) {
        Check.isNotNull(productDocumentService, "productDocumentService");
        Check.isNotNull(storageClient, "storageClient");
        Check.isNotNull(productMapper, "productMapper");

        this.productDocumentService = productDocumentService;
        this.storageClient = storageClient;
        this.productMapper = productMapper;
    }

    @Override
    public ProductDocumentResponse create(final String productEntityId, final String documentEntityId) {
        Check.isNotEmpty(productEntityId, "productEntityId");
        Check.isNotEmpty(documentEntityId, "documentEntityId");

        return buildResponse(productDocumentService.getByEntityId(productEntityId, documentEntityId));
    }

    @Override
    public List<ProductDocumentResponse> createList(final String productEntityId) {
        Check.isNotEmpty(productEntityId, "productEntityId");

        return productDocumentService.listByProductEntityId(productEntityId)
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    private ProductDocumentResponse buildResponse(final ProductDocumentDTO document) {
        // The persisted s3Url may have expired, so a fresh presigned URL is generated on every
        // read instead of trusting a value stored at upload time.
        String presignedUrl = storageClient.getPresignedUrl(document.getS3Key(), false);

        return productMapper.toResponse(document, presignedUrl);
    }
}

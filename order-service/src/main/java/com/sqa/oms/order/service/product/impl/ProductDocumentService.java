package com.sqa.oms.order.service.product.impl;

import com.sqa.oms.order.client.storage.StorageClient;
import com.sqa.oms.order.entity.Product;
import com.sqa.oms.order.entity.ProductDocument;
import com.sqa.oms.order.exception.EntityNotFoundException;
import com.sqa.oms.order.modules.product.dto.ProductDocumentDTO;
import com.sqa.oms.order.modules.product.mapper.IProductMapper;
import com.sqa.oms.order.modules.product.model.ProductDocumentContent;
import com.sqa.oms.order.modules.product.repository.IProductDocumentRepository;
import com.sqa.oms.order.modules.product.repository.IProductRepository;
import com.sqa.oms.order.service.product.IProductDocumentService;
import com.sqa.oms.order.util.Check;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Default implementation of the product document read access.
 */
@Service
@Transactional(readOnly = true)
public class ProductDocumentService implements IProductDocumentService {

    private final IProductRepository productRepository;

    private final IProductDocumentRepository productDocumentRepository;

    private final StorageClient storageClient;

    private final IProductMapper productMapper;

    /**
     * Constructor with dependency injection.
     *
     * @param productRepository         product persistence
     * @param productDocumentRepository document metadata persistence
     * @param storageClient             client of the dedicated file storage service
     * @param productMapper             entity to dto mapper
     */
    public ProductDocumentService(final IProductRepository productRepository,
                                  final IProductDocumentRepository productDocumentRepository,
                                  final StorageClient storageClient,
                                  final IProductMapper productMapper) {
        Check.isNotNull(productRepository, "productRepository");
        Check.isNotNull(productDocumentRepository, "productDocumentRepository");
        Check.isNotNull(storageClient, "storageClient");
        Check.isNotNull(productMapper, "productMapper");

        this.productRepository = productRepository;
        this.productDocumentRepository = productDocumentRepository;
        this.storageClient = storageClient;
        this.productMapper = productMapper;
    }

    @Override
    public ProductDocumentDTO getByEntityId(final String productEntityId, final String documentEntityId) {
        Check.isNotEmpty(productEntityId, "productEntityId");
        Check.isNotEmpty(documentEntityId, "documentEntityId");

        Product product = loadProduct(productEntityId);

        return productMapper.toDto(loadDocument(documentEntityId, product));
    }

    @Override
    public List<ProductDocumentDTO> listByProductEntityId(final String productEntityId) {
        Check.isNotEmpty(productEntityId, "productEntityId");

        Product product = loadProduct(productEntityId);

        return productDocumentRepository.findByProductId(product.getId())
                .stream()
                .map(productMapper::toDto)
                .toList();
    }

    @Override
    public ProductDocumentContent download(final String productEntityId, final String documentEntityId) {
        Check.isNotEmpty(productEntityId, "productEntityId");
        Check.isNotEmpty(documentEntityId, "documentEntityId");

        Product product = loadProduct(productEntityId);
        ProductDocument document = loadDocument(documentEntityId, product);

        String contentType = document.getContentType() == null
                ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                : document.getContentType();

        return new ProductDocumentContent(
                document.getFileName(), contentType, storageClient.downloadFile(document.getS3Key()));
    }

    private Product loadProduct(final String entityId) {
        return productRepository.findByEntityId(entityId)
                .orElseThrow(() -> new EntityNotFoundException("Product " + entityId + " was not found."));
    }

    private ProductDocument loadDocument(final String documentEntityId, final Product product) {
        return productDocumentRepository.findByEntityIdAndProductId(documentEntityId, product.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Document " + documentEntityId + " was not found for product "
                                + product.getEntityId() + "."));
    }
}

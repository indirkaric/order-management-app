package com.sqa.oms.order.service.product.impl;

import com.sqa.oms.common.events.EntityType;
import com.sqa.oms.common.events.EventAction;
import com.sqa.oms.order.client.storage.StorageClient;
import com.sqa.oms.order.entity.Product;
import com.sqa.oms.order.entity.ProductDocument;
import com.sqa.oms.order.event.DomainChangeEvent;
import com.sqa.oms.order.exception.EntityNotFoundException;
import com.sqa.oms.order.modules.product.mapper.IProductMapper;
import com.sqa.oms.order.modules.product.repository.IProductDocumentRepository;
import com.sqa.oms.order.modules.product.repository.IProductRepository;
import com.sqa.oms.order.service.product.ICreateProductDocumentService;
import com.sqa.oms.order.service.security.ICallerContext;
import com.sqa.oms.order.service.validator.ICreateProductDocumentServiceValidator;
import com.sqa.oms.order.util.Check;
import com.sqa.oms.order.util.EntityIdGenerator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;

/**
 * Default implementation of the product document create flow.
 */
@Service
@Transactional
public class CreateProductDocumentService implements ICreateProductDocumentService {

    private final IProductRepository productRepository;

    private final IProductDocumentRepository productDocumentRepository;

    private final StorageClient storageClient;

    private final IProductMapper productMapper;

    private final ICreateProductDocumentServiceValidator validator;

    private final ICallerContext callerContext;

    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Constructor with dependency injection.
     *
     * @param productRepository         product persistence
     * @param productDocumentRepository document metadata persistence
     * @param storageClient             client of the dedicated file storage service
     * @param productMapper             entity to response mapper, used for the event payload
     * @param validator                 validator of the uploaded file
     * @param callerContext             resolved caller identity
     * @param applicationEventPublisher local event publisher
     */
    public CreateProductDocumentService(final IProductRepository productRepository,
                                        final IProductDocumentRepository productDocumentRepository,
                                        final StorageClient storageClient,
                                        final IProductMapper productMapper,
                                        final ICreateProductDocumentServiceValidator validator,
                                        final ICallerContext callerContext,
                                        final ApplicationEventPublisher applicationEventPublisher) {
        Check.isNotNull(productRepository, "productRepository");
        Check.isNotNull(productDocumentRepository, "productDocumentRepository");
        Check.isNotNull(storageClient, "storageClient");
        Check.isNotNull(productMapper, "productMapper");
        Check.isNotNull(validator, "validator");
        Check.isNotNull(callerContext, "callerContext");
        Check.isNotNull(applicationEventPublisher, "applicationEventPublisher");

        this.productRepository = productRepository;
        this.productDocumentRepository = productDocumentRepository;
        this.storageClient = storageClient;
        this.productMapper = productMapper;
        this.validator = validator;
        this.callerContext = callerContext;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * Uploads a document and attaches it to the given product.
     *
     * @param productEntityId entityId of the product the document belongs to
     * @param file            the uploaded file
     * @return entityId of the stored document
     */
    @Override
    public String upload(final String productEntityId, final MultipartFile file) {
        Check.isNotEmpty(productEntityId, "productEntityId");

        validator.validate(file);

        Product product = loadProduct(productEntityId);
        String key = product.getEntityId() + "/" + file.getOriginalFilename();

        // The upload to oms-storage-service happens before anything is written to this service's
        // own database, so a failed upload never leaves behind document metadata with no file.
        storageClient.uploadMultipart(file, key);
        String presignedUrl = storageClient.getPresignedUrl(key, false);

        ProductDocument document = new ProductDocument();
        document.setEntityId(generateDocumentEntityId());
        document.setProduct(product);
        document.setFileName(file.getOriginalFilename());
        document.setContentType(file.getContentType());
        document.setSizeBytes(file.getSize());
        document.setS3Key(key);
        document.setS3Url(presignedUrl);
        document.setUploadedAt(Instant.now());

        ProductDocument saved = productDocumentRepository.save(document);

        applicationEventPublisher.publishEvent(new DomainChangeEvent(
                EntityType.PRODUCT_DOCUMENT, EventAction.CREATE, product.getEntityId(), callerContext.getUserId(),
                productMapper.toResponse(saved, presignedUrl)));

        return saved.getEntityId();
    }

    private String generateDocumentEntityId() {
        String candidate = EntityIdGenerator.generate();
        while (productDocumentRepository.existsByEntityId(candidate)) {
            candidate = EntityIdGenerator.generate();
        }

        return candidate;
    }

    private Product loadProduct(final String entityId) {
        return productRepository.findByEntityId(entityId)
                .orElseThrow(() -> new EntityNotFoundException("Product " + entityId + " was not found."));
    }
}

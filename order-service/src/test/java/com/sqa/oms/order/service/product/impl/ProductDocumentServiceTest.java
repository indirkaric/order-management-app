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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductDocumentServiceTest {

    private static final String MISSING_DOCUMENT_ENTITY_ID = "DOC-MISSING";

    @Mock
    private IProductRepository productRepository;

    @Mock
    private IProductDocumentRepository productDocumentRepository;

    @Mock
    private StorageClient storageClient;

    @Mock
    private IProductMapper productMapper;

    private ProductDocumentService productDocumentService;

    @BeforeEach
    void setUp() {
        productDocumentService = new ProductDocumentService(productRepository, productDocumentRepository,
                storageClient, productMapper);
    }

    @Test
    void shouldThrowWhenDocumentDoesNotBelongToProduct() {
        Product product = new Product();
        product.setId(1L);

        when(productRepository.findByEntityId("PRD-1")).thenReturn(Optional.of(product));
        when(productDocumentRepository.findByEntityIdAndProductId(MISSING_DOCUMENT_ENTITY_ID, 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> productDocumentService.download("PRD-1", MISSING_DOCUMENT_ENTITY_ID))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldFallBackToOctetStreamWhenContentTypeIsMissing() {
        Product product = new Product();
        product.setId(1L);

        ProductDocument document = new ProductDocument();
        document.setFileName("spec.pdf");
        document.setS3Key("PRD-1/spec.pdf");
        document.setContentType(null);

        when(productRepository.findByEntityId("PRD-1")).thenReturn(Optional.of(product));
        when(productDocumentRepository.findByEntityIdAndProductId("DOC-1", 1L)).thenReturn(Optional.of(document));
        when(storageClient.downloadFile("PRD-1/spec.pdf")).thenReturn("bytes".getBytes());

        ProductDocumentContent content = productDocumentService.download("PRD-1", "DOC-1");

        assertThat(content.contentType()).isEqualTo("application/octet-stream");
    }

    @Test
    void shouldThrowNotFoundWhenProductDoesNotExist() {
        when(productRepository.findByEntityId("PRD-MISSING")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productDocumentService.listByProductEntityId("PRD-MISSING"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldMapDocumentsIntoDtos() {
        Product product = new Product();
        product.setId(1L);
        product.setEntityId("PRD-1");

        ProductDocument document = new ProductDocument();
        document.setEntityId("DOC-1");

        ProductDocumentDTO dto = new ProductDocumentDTO();
        dto.setEntityId("DOC-1");

        when(productRepository.findByEntityId("PRD-1")).thenReturn(Optional.of(product));
        when(productDocumentRepository.findByProductId(1L)).thenReturn(java.util.List.of(document));
        when(productMapper.toDto(document)).thenReturn(dto);

        var result = productDocumentService.listByProductEntityId("PRD-1");

        assertThat(result).containsExactly(dto);
    }
}

package com.sqa.oms.order.service.product.impl;

import com.sqa.oms.order.client.storage.StorageClient;
import com.sqa.oms.order.entity.Product;
import com.sqa.oms.order.entity.ProductDocument;
import com.sqa.oms.order.event.DomainChangeEvent;
import com.sqa.oms.order.exception.BusinessException;
import com.sqa.oms.order.exception.EntityNotFoundException;
import com.sqa.oms.order.modules.product.mapper.IProductMapper;
import com.sqa.oms.order.modules.product.model.ProductDocumentResponse;
import com.sqa.oms.order.modules.product.repository.IProductDocumentRepository;
import com.sqa.oms.order.modules.product.repository.IProductRepository;
import com.sqa.oms.order.service.security.ICallerContext;
import com.sqa.oms.order.service.validator.ICreateProductDocumentServiceValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateProductDocumentServiceTest {

    private static final long FILE_SIZE_BYTES = 7L;

    @Mock
    private IProductRepository productRepository;

    @Mock
    private IProductDocumentRepository productDocumentRepository;

    @Mock
    private StorageClient storageClient;

    @Mock
    private IProductMapper productMapper;

    @Mock
    private ICreateProductDocumentServiceValidator validator;

    @Mock
    private ICallerContext callerContext;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private CreateProductDocumentService createProductDocumentService;

    @BeforeEach
    void setUp() {
        createProductDocumentService = new CreateProductDocumentService(productRepository,
                productDocumentRepository, storageClient, productMapper, validator, callerContext,
                applicationEventPublisher);
    }

    @Test
    void shouldUploadDocumentBeforeWritingMetadata() {
        Product product = new Product();
        product.setId(1L);
        product.setEntityId("PRD-1");

        MockMultipartFile file = new MockMultipartFile("file", "spec.pdf",
                "application/pdf", "content".getBytes());

        when(productRepository.findByEntityId("PRD-1")).thenReturn(Optional.of(product));
        when(storageClient.getPresignedUrl(eq("PRD-1/spec.pdf"), eq(false))).thenReturn("https://presigned-url");
        when(productDocumentRepository.save(any(ProductDocument.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(productMapper.toResponse(any(ProductDocument.class), eq("https://presigned-url"))).thenReturn(
                new ProductDocumentResponse("DOC-1", "PRD-1", "spec.pdf", "application/pdf", FILE_SIZE_BYTES,
                        "https://presigned-url", null));

        createProductDocumentService.upload("PRD-1", file);

        verify(storageClient).uploadMultipart(file, "PRD-1/spec.pdf");
        verify(productDocumentRepository).save(any(ProductDocument.class));
        verify(applicationEventPublisher).publishEvent(any(DomainChangeEvent.class));
    }

    @Test
    void shouldRejectUploadDocumentWhenFileIsEmpty() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "spec.pdf",
                "application/pdf", new byte[0]);
        doThrow(new BusinessException("The uploaded file must not be empty."))
                .when(validator).validate(emptyFile);

        assertThatThrownBy(() -> createProductDocumentService.upload("PRD-1", emptyFile))
                .isInstanceOf(BusinessException.class);

        verify(storageClient, never()).uploadMultipart(any(), any());
    }

    @Test
    void shouldThrowNotFoundWhenProductDoesNotExist() {
        MockMultipartFile file = new MockMultipartFile("file", "spec.pdf",
                "application/pdf", "content".getBytes());

        when(productRepository.findByEntityId("PRD-MISSING")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> createProductDocumentService.upload("PRD-MISSING", file))
                .isInstanceOf(EntityNotFoundException.class);
    }
}

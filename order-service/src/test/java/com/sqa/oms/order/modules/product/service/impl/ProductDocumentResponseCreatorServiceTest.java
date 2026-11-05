package com.sqa.oms.order.modules.product.service.impl;

import com.sqa.oms.order.client.storage.StorageClient;
import com.sqa.oms.order.modules.product.dto.ProductDocumentDTO;
import com.sqa.oms.order.modules.product.mapper.IProductMapper;
import com.sqa.oms.order.modules.product.model.ProductDocumentResponse;
import com.sqa.oms.order.service.product.IProductDocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductDocumentResponseCreatorServiceTest {

    private static final long FILE_SIZE_BYTES = 7L;

    @Mock
    private IProductDocumentService productDocumentService;

    @Mock
    private StorageClient storageClient;

    @Mock
    private IProductMapper productMapper;

    private ProductDocumentResponseCreatorService responseCreatorService;

    @BeforeEach
    void setUp() {
        responseCreatorService = new ProductDocumentResponseCreatorService(productDocumentService, storageClient,
                productMapper);
    }

    @Test
    void shouldBuildTheSingleResponseWithAFreshPresignedUrl() {
        ProductDocumentDTO dto = new ProductDocumentDTO();
        dto.setEntityId("DOC-1");
        dto.setS3Key("PRD-1/spec.pdf");
        ProductDocumentResponse response = new ProductDocumentResponse(
                "DOC-1", "PRD-1", "spec.pdf", "application/pdf", FILE_SIZE_BYTES, "https://presigned-url", null);

        when(productDocumentService.getByEntityId("PRD-1", "DOC-1")).thenReturn(dto);
        when(storageClient.getPresignedUrl("PRD-1/spec.pdf", false)).thenReturn("https://presigned-url");
        when(productMapper.toResponse(dto, "https://presigned-url")).thenReturn(response);

        ProductDocumentResponse result = responseCreatorService.create("PRD-1", "DOC-1");

        assertThat(result).isEqualTo(response);
    }

    @Test
    void shouldBuildTheListResponseWithAFreshPresignedUrlPerDocument() {
        ProductDocumentDTO dto = new ProductDocumentDTO();
        dto.setEntityId("DOC-1");
        dto.setS3Key("PRD-1/spec.pdf");
        ProductDocumentResponse response = new ProductDocumentResponse(
                "DOC-1", "PRD-1", "spec.pdf", "application/pdf", FILE_SIZE_BYTES, "https://presigned-url", null);

        when(productDocumentService.listByProductEntityId("PRD-1")).thenReturn(List.of(dto));
        when(storageClient.getPresignedUrl("PRD-1/spec.pdf", false)).thenReturn("https://presigned-url");
        when(productMapper.toResponse(dto, "https://presigned-url")).thenReturn(response);

        List<ProductDocumentResponse> result = responseCreatorService.createList("PRD-1");

        assertThat(result).containsExactly(response);
    }
}

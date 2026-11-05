package com.sqa.oms.order.modules.product.service.impl;

import com.sqa.oms.order.modules.product.model.ProductDocumentResponse;
import com.sqa.oms.order.modules.product.service.IProductDocumentResponseCreatorService;
import com.sqa.oms.order.service.product.ICreateProductDocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateProductDocumentResponseCreatorServiceTest {

    private static final long FILE_SIZE_BYTES = 7L;

    @Mock
    private ICreateProductDocumentService createProductDocumentService;

    @Mock
    private IProductDocumentResponseCreatorService productDocumentResponseCreatorService;

    private CreateProductDocumentResponseCreatorService responseCreatorService;

    @BeforeEach
    void setUp() {
        responseCreatorService = new CreateProductDocumentResponseCreatorService(createProductDocumentService,
                productDocumentResponseCreatorService);
    }

    @Test
    void shouldUploadThenBuildTheResponseFromTheNewEntityId() {
        MockMultipartFile file = new MockMultipartFile("file", "spec.pdf", "application/pdf", "content".getBytes());
        ProductDocumentResponse response = new ProductDocumentResponse(
                "DOC-1", "PRD-1", "spec.pdf", "application/pdf", FILE_SIZE_BYTES, "https://presigned-url", null);

        when(createProductDocumentService.upload("PRD-1", file)).thenReturn("DOC-1");
        when(productDocumentResponseCreatorService.create("PRD-1", "DOC-1")).thenReturn(response);

        ProductDocumentResponse result = responseCreatorService.create("PRD-1", file);

        assertThat(result).isEqualTo(response);
    }
}

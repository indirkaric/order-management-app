package com.sqa.oms.ui.modules.order;

import com.sqa.oms.ui.client.response.ClientFileResponseWrapper;
import com.sqa.oms.ui.modules.order.model.CreateProductRequest;
import com.sqa.oms.ui.modules.order.model.ProductDocumentResponse;
import com.sqa.oms.ui.modules.order.model.ProductListRequest;
import com.sqa.oms.ui.modules.order.model.ProductListResponse;
import com.sqa.oms.ui.modules.order.model.ProductResponse;
import com.sqa.oms.ui.service.order.IProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    private static final int PAGE_SIZE = 20;

    private static final long FILE_SIZE_BYTES = 7L;

    @Mock
    private IProductService productService;

    private ProductController productController;

    @BeforeEach
    void setUp() {
        productController = new ProductController(productService);
    }

    @Test
    void shouldReturn201OnCreateProduct() {
        CreateProductRequest request = new CreateProductRequest("Keyboard", "KBD-1", "desc", BigDecimal.TEN);
        ProductResponse response = new ProductResponse(1L, "PRD-1", "Keyboard", "KBD-1", "desc", BigDecimal.TEN);
        when(productService.create(request)).thenReturn(response);

        ResponseEntity<ProductResponse> result = productController.create(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void shouldReturn201OnUploadDocument() {
        MockMultipartFile file = new MockMultipartFile("file", "spec.pdf", "application/pdf", "content".getBytes());
        ProductDocumentResponse response = new ProductDocumentResponse(1L, 1L, "spec.pdf", "application/pdf",
                FILE_SIZE_BYTES, "https://presigned-url", null);
        when(productService.uploadDocument("1", file)).thenReturn(response);

        ResponseEntity<ProductDocumentResponse> result = productController.uploadDocument("1", file);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void shouldSetContentDispositionWhenMissingFromDownstreamOnDownloadDocument() {
        ClientFileResponseWrapper content = new ClientFileResponseWrapper(
                HttpStatus.OK, "bytes".getBytes(), "application/pdf", null);
        when(productService.downloadDocument("1", "2")).thenReturn(content);

        ResponseEntity<byte[]> result = productController.downloadDocument("1", "2");

        assertThat(result.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)).contains("attachment");
        assertThat(result.getBody()).isEqualTo("bytes".getBytes());
    }

    @Test
    void shouldDelegateToTheServiceWhenGettingAll() {
        ProductResponse product = new ProductResponse(1L, "PRD-1", "Keyboard", "KBD-1", "desc", BigDecimal.TEN);
        ProductListResponse response = new ProductListResponse(List.of(product), 1);
        ProductListRequest request = new ProductListRequest(null, null);
        when(productService.getAll(request, PageRequest.of(0, PAGE_SIZE))).thenReturn(response);

        assertThat(productController.getAll(request, PageRequest.of(0, PAGE_SIZE)).getBody()).isEqualTo(response);
    }

    @Test
    void shouldReturnNoContentWhenNoProductsAreFound() {
        ProductListResponse response = new ProductListResponse(List.of(), 0);
        ProductListRequest request = new ProductListRequest(null, null);
        when(productService.getAll(request, PageRequest.of(0, PAGE_SIZE))).thenReturn(response);

        assertThat(productController.getAll(request, PageRequest.of(0, PAGE_SIZE)).getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void shouldDelegateToTheServiceWhenGettingDocuments() {
        List<ProductDocumentResponse> documents = List.of(new ProductDocumentResponse(1L, 1L, "spec.pdf",
                "application/pdf", FILE_SIZE_BYTES, "https://presigned-url", null));
        when(productService.getDocuments("1")).thenReturn(documents);

        assertThat(productController.getDocuments("1").getBody()).isEqualTo(documents);
    }
}

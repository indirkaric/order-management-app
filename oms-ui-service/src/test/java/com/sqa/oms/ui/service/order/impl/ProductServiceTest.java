package com.sqa.oms.ui.service.order.impl;

import com.sqa.oms.ui.client.order.ProductClient;
import com.sqa.oms.ui.client.response.ClientFileResponseWrapper;
import com.sqa.oms.ui.client.response.ClientResponseWrapper;
import com.sqa.oms.ui.modules.order.model.CreateProductRequest;
import com.sqa.oms.ui.modules.order.model.ProductDocumentResponse;
import com.sqa.oms.ui.modules.order.model.ProductListRequest;
import com.sqa.oms.ui.modules.order.model.ProductListResponse;
import com.sqa.oms.ui.modules.order.model.ProductResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    private static final int PAGE_SIZE = 20;

    private static final long FILE_SIZE_BYTES = 7L;

    @Mock
    private ProductClient productClient;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productClient);
    }

    @Test
    void shouldForwardTheFileAndReturnTheCreatedDocumentOnUploadDocument() {
        MockMultipartFile file = new MockMultipartFile("file", "spec.pdf", "application/pdf", "content".getBytes());
        ProductDocumentResponse response = new ProductDocumentResponse(1L, 1L, "spec.pdf", "application/pdf",
                FILE_SIZE_BYTES, "https://presigned-url", null);

        when(productClient.uploadProductDocument("1", file))
                .thenReturn(new ClientResponseWrapper(HttpStatus.CREATED, response));

        assertThat(productService.uploadDocument("1", file)).isEqualTo(response);
    }

    @Test
    void shouldReturnTheRawBytesAndHeadersOnDownloadDocument() {
        ClientFileResponseWrapper wrapper = new ClientFileResponseWrapper(
                HttpStatus.OK, "content".getBytes(), "application/pdf", "attachment; filename=\"spec.pdf\"");
        when(productClient.downloadProductDocument("1", "2")).thenReturn(wrapper);

        ClientFileResponseWrapper result = productService.downloadDocument("1", "2");

        assertThat(result.getContentType()).isEqualTo("application/pdf");
        assertThat(result.getResponseBody()).isEqualTo("content".getBytes());
    }

    @Test
    void shouldReturnTheMappedProductWhenGettingById() {
        ProductResponse response = new ProductResponse(1L, "PRD-1", "Keyboard", "KBD-1", "desc", BigDecimal.TEN);

        when(productClient.getProduct("1")).thenReturn(new ClientResponseWrapper(HttpStatus.OK, response));

        assertThat(productService.getById("1")).isEqualTo(response);
    }

    @Test
    void shouldForwardTheRequestOnCreate() {
        CreateProductRequest request = new CreateProductRequest("Keyboard", "KBD-1", "desc", BigDecimal.TEN);
        ProductResponse response = new ProductResponse(1L, "PRD-1", "Keyboard", "KBD-1", "desc", BigDecimal.TEN);

        when(productClient.createProduct(request)).thenReturn(new ClientResponseWrapper(HttpStatus.CREATED,
                response));

        assertThat(productService.create(request)).isEqualTo(response);
    }

    @Test
    void shouldForwardToTheClientWhenGettingDocuments() {
        ProductDocumentResponse[] documents = {new ProductDocumentResponse(1L, 1L, "spec.pdf", "application/pdf",
                FILE_SIZE_BYTES, "https://presigned-url", null)};

        when(productClient.getProductDocuments("1")).thenReturn(new ClientResponseWrapper(HttpStatus.OK,
                documents));

        assertThat(productService.getDocuments("1")).containsExactly(documents);
    }

    @Test
    void shouldForwardTheQueryParamsWhenGettingAll() {
        ProductListResponse response = new ProductListResponse();
        response.setData(List.of());
        ProductListRequest request = new ProductListRequest(null, null);

        when(productClient.getProducts(any(), any())).thenReturn(new ClientResponseWrapper(HttpStatus.OK, response));

        assertThat(productService.getAll(request, PageRequest.of(0, PAGE_SIZE))).isEqualTo(response);
    }

    @Test
    void shouldReturnAnEmptyResponseWhenTheOrderServiceReturnsNoContentOnGettingAll() {
        ProductListRequest request = new ProductListRequest(null, null);

        when(productClient.getProducts(any(), any()))
                .thenReturn(new ClientResponseWrapper(HttpStatus.NO_CONTENT, null));

        assertThat(productService.getAll(request, PageRequest.of(0, PAGE_SIZE)).getData()).isEmpty();
    }
}

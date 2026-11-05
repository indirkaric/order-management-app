package com.sqa.oms.order.modules.product;

import com.sqa.oms.order.modules.product.model.CreateProductRequest;
import com.sqa.oms.order.modules.product.model.ProductDocumentContent;
import com.sqa.oms.order.modules.product.model.ProductDocumentResponse;
import com.sqa.oms.order.modules.product.model.ProductListRequest;
import com.sqa.oms.order.modules.product.model.ProductListResponse;
import com.sqa.oms.order.modules.product.model.ProductResponse;
import com.sqa.oms.order.modules.product.service.ICreateProductDocumentResponseCreatorService;
import com.sqa.oms.order.modules.product.service.ICreateProductResponseCreatorService;
import com.sqa.oms.order.modules.product.service.IProductDocumentResponseCreatorService;
import com.sqa.oms.order.modules.product.service.IProductListResponseCreatorService;
import com.sqa.oms.order.modules.product.service.IProductResponseCreatorService;
import com.sqa.oms.order.service.product.IProductDocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
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
    private ICreateProductResponseCreatorService createProductResponseCreatorService;

    @Mock
    private ICreateProductDocumentResponseCreatorService createProductDocumentResponseCreatorService;

    @Mock
    private IProductResponseCreatorService productResponseCreatorService;

    @Mock
    private IProductListResponseCreatorService productListResponseCreatorService;

    @Mock
    private IProductDocumentResponseCreatorService productDocumentResponseCreatorService;

    @Mock
    private IProductDocumentService productDocumentService;

    private ProductController productController;

    @BeforeEach
    void setUp() {
        productController = new ProductController(createProductResponseCreatorService,
                createProductDocumentResponseCreatorService, productResponseCreatorService,
                productListResponseCreatorService, productDocumentResponseCreatorService, productDocumentService);
    }

    @Test
    void shouldReturn201WhenProductIsCreated() {
        CreateProductRequest request = new CreateProductRequest("Keyboard", "KBD-1", "desc", BigDecimal.TEN);
        ProductResponse response = new ProductResponse("PRD-1", "Keyboard", "KBD-1", "desc", BigDecimal.TEN);
        when(createProductResponseCreatorService.create(request)).thenReturn(response);

        ResponseEntity<ProductResponse> result = productController.createProduct(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void shouldDelegateListProductsToTheResponseCreator() {
        ProductResponse response = new ProductResponse("PRD-1", "Keyboard", "KBD-1", "desc", BigDecimal.TEN);
        ProductListRequest request = new ProductListRequest(null, null);
        ProductListResponse listResponse = new ProductListResponse(List.of(response), 1L);
        when(productListResponseCreatorService.create(request, PageRequest.of(0, PAGE_SIZE)))
                .thenReturn(listResponse);

        ResponseEntity<ProductListResponse> result =
                productController.listProducts(request, PageRequest.of(0, PAGE_SIZE));

        assertThat(result.getBody()).isEqualTo(listResponse);
    }

    @Test
    void shouldReturnTheResponseCreatorResponseForGetProduct() {
        ProductResponse response = new ProductResponse("PRD-1", "Keyboard", "KBD-1", "desc", BigDecimal.TEN);
        when(productResponseCreatorService.create("PRD-1")).thenReturn(response);

        ResponseEntity<ProductResponse> result = productController.getProduct("PRD-1");

        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void shouldReturn201WhenDocumentIsUploaded() {
        MockMultipartFile file = new MockMultipartFile("file", "spec.pdf", "application/pdf", "content".getBytes());
        ProductDocumentResponse response = new ProductDocumentResponse(
                "DOC-1", "PRD-1", "spec.pdf", "application/pdf", FILE_SIZE_BYTES, "https://presigned-url", null);
        when(createProductDocumentResponseCreatorService.create("PRD-1", file)).thenReturn(response);

        ResponseEntity<ProductDocumentResponse> result = productController.uploadDocument("PRD-1", file);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void shouldDelegateListDocumentsToTheResponseCreator() {
        ProductDocumentResponse response = new ProductDocumentResponse(
                "DOC-1", "PRD-1", "spec.pdf", "application/pdf", FILE_SIZE_BYTES, "https://presigned-url", null);
        when(productDocumentResponseCreatorService.createList("PRD-1")).thenReturn(List.of(response));

        ResponseEntity<List<ProductDocumentResponse>> result = productController.listDocuments("PRD-1");

        assertThat(result.getBody()).containsExactly(response);
    }

    @Test
    void shouldSetContentDispositionAndTypeWhenDownloadingDocument() {
        ProductDocumentContent content = new ProductDocumentContent(
                "spec.pdf", "application/pdf", "content".getBytes());
        when(productDocumentService.download("PRD-1", "DOC-1")).thenReturn(content);

        ResponseEntity<byte[]> result = productController.downloadDocument("PRD-1", "DOC-1");

        assertThat(result.getHeaders().getFirst(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION))
                .contains("spec.pdf");
        assertThat(result.getBody()).isEqualTo(content.content());
    }
}

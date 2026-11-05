package com.sqa.oms.ui.modules.order;

import com.sqa.oms.ui.client.response.ClientFileResponseWrapper;
import com.sqa.oms.ui.constants.Constants;
import com.sqa.oms.ui.modules.order.model.CreateProductRequest;
import com.sqa.oms.ui.modules.order.model.ProductDocumentResponse;
import com.sqa.oms.ui.modules.order.model.ProductListRequest;
import com.sqa.oms.ui.modules.order.model.ProductListResponse;
import com.sqa.oms.ui.modules.order.model.ProductResponse;
import com.sqa.oms.ui.service.order.IProductService;
import com.sqa.oms.ui.util.Check;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Proxies the product resources of order-service, including document upload/download.
 */
@RestController
@RequestMapping(path = "/api/v1/products", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Product Controller", description = "Proxy to order-service product APIs.")
public class ProductController {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final IProductService productService;

    /**
     * Constructor with dependency injection.
     *
     * @param productService proxy service
     */
    public ProductController(final IProductService productService) {
        Check.isNotNull(productService, "productService");
        this.productService = productService;
    }

    /**
     * Searches catalog products.
     *
     * @param request  the query parameters
     * @param pageable the paging information
     * @return a page of products
     */
    @Operation(
            summary = "Search products",
            description = "Returns a filtered and paged list of catalog products",
            security = {@SecurityRequirement(name = Constants.BEARER_KEY_VALUE)})
    @ApiResponses(
            value = {@ApiResponse(
                    responseCode = "200",
                    description = "Page of products",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProductListResponse.class))}),
                    @ApiResponse(responseCode = "204", description = "No products are found", content = @Content)})
    @GetMapping
    @PreAuthorize(Constants.Authority.PRODUCT_READ_AUTHORITY)
    public ResponseEntity<ProductListResponse> getAll(
            @ParameterObject final ProductListRequest request,
            @ParameterObject @PageableDefault(size = DEFAULT_PAGE_SIZE) final Pageable pageable) {
        ProductListResponse response = productService.getAll(request, pageable);

        if (response.getData().isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Returns a single product.
     *
     * @param id product identifier
     * @return the product
     */
    @Operation(
            summary = "Get a single product",
            description = "Returns one catalog product",
            security = {@SecurityRequirement(name = Constants.BEARER_KEY_VALUE)})
    @ApiResponses(
            value = {@ApiResponse(
                    responseCode = "200",
                    description = "The product",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProductResponse.class))}),
                    @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)})
    @GetMapping("/{id}")
    @PreAuthorize(Constants.Authority.PRODUCT_READ_AUTHORITY)
    public ResponseEntity<ProductResponse> getById(@PathVariable final String id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    /**
     * Creates a product.
     *
     * @param request request body
     * @return the created product
     */
    @Operation(
            summary = "Create a product",
            description = "Creates a new catalog product",
            security = {@SecurityRequirement(name = Constants.BEARER_KEY_VALUE)})
    @ApiResponses(
            value = {@ApiResponse(
                    responseCode = "201",
                    description = "Product created",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProductResponse.class))}),
                    @ApiResponse(responseCode = "422", description = "Invalid input, or sku already taken",
                            content = @Content)})
    @PostMapping
    @PreAuthorize(Constants.Authority.PRODUCT_WRITE_AUTHORITY)
    public ResponseEntity<ProductResponse> create(@RequestBody final CreateProductRequest request) {
        return new ResponseEntity<>(productService.create(request), HttpStatus.CREATED);
    }

    /**
     * Uploads a documentation file for a product.
     *
     * @param id   product identifier
     * @param file inbound multipart file
     * @return the created document
     */
    @Operation(
            summary = "Upload a product document",
            description = "Uploads a documentation file for a product to the storage service and stores its "
                    + "metadata",
            security = {@SecurityRequirement(name = Constants.BEARER_KEY_VALUE)})
    @ApiResponses(
            value = {@ApiResponse(
                    responseCode = "201",
                    description = "Document stored",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProductDocumentResponse.class))}),
                    @ApiResponse(responseCode = "404", description = "Product not found", content = @Content),
                    @ApiResponse(responseCode = "422", description = "Empty file", content = @Content)})
    @PostMapping(path = "/{id}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize(Constants.Authority.PRODUCT_WRITE_AUTHORITY)
    public ResponseEntity<ProductDocumentResponse> uploadDocument(@PathVariable final String id,
                                                                    @RequestParam("file")
                                                                    final MultipartFile file) {
        return new ResponseEntity<>(productService.uploadDocument(id, file), HttpStatus.CREATED);
    }

    /**
     * Lists the documents uploaded for a product.
     *
     * @param id product identifier
     * @return the document list
     */
    @Operation(
            summary = "List the documents of a product",
            description = "Lists every document uploaded for a product, each with a freshly generated "
                    + "presigned download URL",
            security = {@SecurityRequirement(name = Constants.BEARER_KEY_VALUE)})
    @ApiResponses(
            value = {@ApiResponse(
                    responseCode = "200",
                    description = "The documents",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProductDocumentResponse.class))}),
                    @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)})
    @GetMapping("/{id}/documents")
    @PreAuthorize(Constants.Authority.PRODUCT_READ_AUTHORITY)
    public ResponseEntity<List<ProductDocumentResponse>> getDocuments(@PathVariable final String id) {
        return ResponseEntity.ok(productService.getDocuments(id));
    }

    /**
     * Downloads a product document, streaming the bytes straight back to the caller.
     *
     * @param id         product identifier
     * @param documentId document identifier
     * @return the file content
     */
    @Operation(
            summary = "Download a product document",
            description = "Downloads the raw bytes of a previously uploaded document",
            security = {@SecurityRequirement(name = Constants.BEARER_KEY_VALUE)})
    @ApiResponses(
            value = {@ApiResponse(
                    responseCode = "200",
                    description = "The file content",
                    content = {@Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)}),
                    @ApiResponse(responseCode = "404", description = "Product or document not found",
                            content = @Content)})
    @GetMapping("/{id}/documents/{documentId}/download")
    @PreAuthorize(Constants.Authority.PRODUCT_READ_AUTHORITY)
    public ResponseEntity<byte[]> downloadDocument(@PathVariable final String id,
                                                    @PathVariable final String documentId) {
        ClientFileResponseWrapper content = productService.downloadDocument(id, documentId);

        ResponseEntity.BodyBuilder response = ResponseEntity.ok();

        if (content.getContentType() != null) {
            response.contentType(MediaType.parseMediaType(content.getContentType()));
        }

        if (content.getContentDisposition() != null) {
            response.header(HttpHeaders.CONTENT_DISPOSITION, content.getContentDisposition());
        } else {
            response.header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().build().toString());
        }

        return response.body(content.getResponseBody());
    }
}

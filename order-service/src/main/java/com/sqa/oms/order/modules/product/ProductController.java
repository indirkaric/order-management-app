package com.sqa.oms.order.modules.product;

import com.sqa.oms.order.constants.Constants;
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
import com.sqa.oms.order.util.Check;
import com.sqa.oms.order.util.CustomFieldProcessor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
 * CRUD API over the product catalog and its uploaded documentation files.
 */
@RestController
@RequestMapping(path = "/api/v1/products", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Product Controller", description = "Product and product documentation APIs.")
public class ProductController {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final ICreateProductResponseCreatorService createProductResponseCreatorService;

    private final ICreateProductDocumentResponseCreatorService createProductDocumentResponseCreatorService;

    private final IProductResponseCreatorService productResponseCreatorService;

    private final IProductListResponseCreatorService productListResponseCreatorService;

    private final IProductDocumentResponseCreatorService productDocumentResponseCreatorService;

    private final IProductDocumentService productDocumentService;

    /**
     * Constructor with dependency injection.
     *
     * @param createProductResponseCreatorService         builds the response of the create endpoint
     * @param createProductDocumentResponseCreatorService builds the response of the upload endpoint
     * @param productResponseCreatorService                builds the single product response
     * @param productListResponseCreatorService             builds the product list response
     * @param productDocumentResponseCreatorService         builds the single and list document responses
     * @param productDocumentService                        core product document read access, for raw downloads
     */
    public ProductController(final ICreateProductResponseCreatorService createProductResponseCreatorService,
                             final ICreateProductDocumentResponseCreatorService
                                     createProductDocumentResponseCreatorService,
                             final IProductResponseCreatorService productResponseCreatorService,
                             final IProductListResponseCreatorService productListResponseCreatorService,
                             final IProductDocumentResponseCreatorService productDocumentResponseCreatorService,
                             final IProductDocumentService productDocumentService) {
        Check.isNotNull(createProductResponseCreatorService, "createProductResponseCreatorService");
        Check.isNotNull(createProductDocumentResponseCreatorService, "createProductDocumentResponseCreatorService");
        Check.isNotNull(productResponseCreatorService, "productResponseCreatorService");
        Check.isNotNull(productListResponseCreatorService, "productListResponseCreatorService");
        Check.isNotNull(productDocumentResponseCreatorService, "productDocumentResponseCreatorService");
        Check.isNotNull(productDocumentService, "productDocumentService");

        this.createProductResponseCreatorService = createProductResponseCreatorService;
        this.createProductDocumentResponseCreatorService = createProductDocumentResponseCreatorService;
        this.productResponseCreatorService = productResponseCreatorService;
        this.productListResponseCreatorService = productListResponseCreatorService;
        this.productDocumentResponseCreatorService = productDocumentResponseCreatorService;
        this.productDocumentService = productDocumentService;
    }

    /**
     * Creates a new catalog product.
     *
     * @param request create request
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
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody final CreateProductRequest request) {
        Check.isNotNull(request, "request");

        return new ResponseEntity<>(createProductResponseCreatorService.create(request), HttpStatus.CREATED);
    }

    /**
     * Searches catalog products.
     *
     * @param request  the query parameters
     * @param pageable paging information
     * @return paged list of products
     */
    @Operation(
            summary = "Search products",
            description = "Returns a filtered and paged list of catalog products",
            security = {@SecurityRequirement(name = Constants.BEARER_KEY_VALUE)})
    @ApiResponses(
            value = {@ApiResponse(
                    responseCode = "200",
                    description = "Paged list of products",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProductListResponse.class))}),
                    @ApiResponse(responseCode = "204", description = "No products are found", content = @Content)})
    @GetMapping
    @Parameters({
            @Parameter(name = "request", hidden = true),
            @Parameter(name = "pageable", hidden = true),
            @Parameter(
                    name = "sku",
                    schema = @Schema(type = "string"),
                    in = ParameterIn.QUERY,
                    description = "Sku to filter data."),
            @Parameter(
                    name = "textSearch",
                    schema = @Schema(type = "string"),
                    in = ParameterIn.QUERY,
                    description = "Text search for name and description."),
            @Parameter(
                    name = "page",
                    schema = @Schema(type = "integer", example = "0", defaultValue = "0"),
                    in = ParameterIn.QUERY,
                    description = "Page number to retrieve."),
            @Parameter(
                    name = "size",
                    schema = @Schema(type = "integer", example = "20", defaultValue = "20"),
                    in = ParameterIn.QUERY,
                    description = "Page size to retrieve."),
            @Parameter(
                    name = "sort",
                    schema = @Schema(example = "name", defaultValue = ""),
                    in = ParameterIn.QUERY,
                    description = "Name of the field to sort data.")
    })
    @PreAuthorize(Constants.Authority.PRODUCT_READ_AUTHORITY)
    public ResponseEntity<ProductListResponse> listProducts(
            final ProductListRequest request,
            @PageableDefault(size = DEFAULT_PAGE_SIZE) final Pageable pageable) {

        Check.isNotNull(request, "request");
        Check.isNotNull(pageable, "pageable");

        CustomFieldProcessor.processAnnotatedFields(request);

        ProductListResponse response = productListResponseCreatorService.create(request, pageable);

        if (response.getData().isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Returns a single product.
     *
     * @param entityId business key of the product
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
    public ResponseEntity<ProductResponse> getProduct(@PathVariable("id") final String entityId) {
        Check.isNotEmpty(entityId, "entityId");

        return ResponseEntity.ok(productResponseCreatorService.create(entityId));
    }

    /**
     * Uploads a documentation file for a product.
     *
     * @param entityId business key of the product
     * @param file     multipart file
     * @return the stored document metadata
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
    public ResponseEntity<ProductDocumentResponse> uploadDocument(
            @PathVariable("id") final String entityId, @RequestParam("file") final MultipartFile file) {
        Check.isNotEmpty(entityId, "entityId");
        Check.isNotNull(file, "file");

        return new ResponseEntity<>(
                createProductDocumentResponseCreatorService.create(entityId, file), HttpStatus.CREATED);
    }

    /**
     * Lists the documents uploaded for a product.
     *
     * @param entityId business key of the product
     * @return the documents
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
    public ResponseEntity<List<ProductDocumentResponse>> listDocuments(
            @PathVariable("id") final String entityId) {
        Check.isNotEmpty(entityId, "entityId");

        return ResponseEntity.ok(productDocumentResponseCreatorService.createList(entityId));
    }

    /**
     * Downloads the raw bytes of a document.
     *
     * @param entityId         business key of the product
     * @param documentEntityId business key of the document
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
    public ResponseEntity<byte[]> downloadDocument(@PathVariable("id") final String entityId,
                                                          @PathVariable("documentId") final String documentEntityId) {
        Check.isNotEmpty(entityId, "entityId");
        Check.isNotEmpty(documentEntityId, "documentEntityId");

        ProductDocumentContent content = productDocumentService.download(entityId, documentEntityId);

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(content.fileName())
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.parseMediaType(content.contentType()))
                .body(content.content());
    }
}

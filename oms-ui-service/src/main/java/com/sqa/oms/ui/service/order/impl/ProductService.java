package com.sqa.oms.ui.service.order.impl;

import com.sqa.oms.ui.client.order.ProductClient;
import com.sqa.oms.ui.client.response.ClientFileResponseWrapper;
import com.sqa.oms.ui.constants.Constants;
import com.sqa.oms.ui.modules.order.model.CreateProductRequest;
import com.sqa.oms.ui.modules.order.model.ProductDocumentResponse;
import com.sqa.oms.ui.modules.order.model.ProductListRequest;
import com.sqa.oms.ui.modules.order.model.ProductListResponse;
import com.sqa.oms.ui.modules.order.model.ProductResponse;
import com.sqa.oms.ui.service.order.IProductService;
import com.sqa.oms.ui.util.Check;
import com.sqa.oms.ui.util.ResponseUtil;
import lombok.SneakyThrows;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Forwards every product call to the order service, which owns the storage integration.
 */
@Service
public class ProductService implements IProductService {

    private final ProductClient productClient;

    /**
     * Constructor with dependency injection.
     *
     * @param productClient product client of the order service
     */
    public ProductService(final ProductClient productClient) {
        Check.isNotNull(productClient, "productClient");
        this.productClient = productClient;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SneakyThrows
    public ProductListResponse getAll(final ProductListRequest request, final Pageable pageable) {
        Check.isNotNull(request, "request");
        Check.isNotNull(pageable, "pageable");

        ProductListResponse response = ResponseUtil.handleResponse(productClient.getProducts(request, pageable),
                ProductListResponse.class, Constants.ORDER_SERVICE_NAME);

        return response == null ? new ProductListResponse() : response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SneakyThrows
    public ProductResponse getById(final String productId) {
        Check.isNotEmpty(productId, "productId");
        return ResponseUtil.handleResponse(productClient.getProduct(productId), ProductResponse.class,
                Constants.ORDER_SERVICE_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SneakyThrows
    public ProductResponse create(final CreateProductRequest request) {
        Check.isNotNull(request, "request");
        return ResponseUtil.handleResponse(productClient.createProduct(request), ProductResponse.class,
                Constants.ORDER_SERVICE_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SneakyThrows
    public List<ProductDocumentResponse> getDocuments(final String productId) {
        Check.isNotEmpty(productId, "productId");
        return List.of(ResponseUtil.handleResponse(productClient.getProductDocuments(productId),
                ProductDocumentResponse[].class, Constants.ORDER_SERVICE_NAME));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SneakyThrows
    public ProductDocumentResponse uploadDocument(final String productId, final MultipartFile file) {
        Check.isNotEmpty(productId, "productId");
        Check.isNotNull(file, "file");
        return ResponseUtil.handleResponse(productClient.uploadProductDocument(productId, file),
                ProductDocumentResponse.class, Constants.ORDER_SERVICE_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SneakyThrows
    public ClientFileResponseWrapper downloadDocument(final String productId, final String documentId) {
        Check.isNotEmpty(productId, "productId");
        Check.isNotEmpty(documentId, "documentId");
        return ResponseUtil.handleFileResponse(productClient.downloadProductDocument(productId, documentId),
                Constants.ORDER_SERVICE_NAME);
    }
}

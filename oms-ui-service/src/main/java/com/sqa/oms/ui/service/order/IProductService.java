package com.sqa.oms.ui.service.order;

import com.sqa.oms.ui.client.response.ClientFileResponseWrapper;
import com.sqa.oms.ui.modules.order.model.CreateProductRequest;
import com.sqa.oms.ui.modules.order.model.ProductDocumentResponse;
import com.sqa.oms.ui.modules.order.model.ProductListRequest;
import com.sqa.oms.ui.modules.order.model.ProductListResponse;
import com.sqa.oms.ui.modules.order.model.ProductResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Proxy to the product resources of the order service.
 */
public interface IProductService {

    /**
     * Retrieves the product list.
     *
     * @param request  list filters
     * @param pageable paging information
     * @return product list
     */
    ProductListResponse getAll(ProductListRequest request, Pageable pageable);

    /**
     * Retrieves a single product.
     *
     * @param productId product identifier
     * @return product
     */
    ProductResponse getById(String productId);

    /**
     * Creates a product.
     *
     * @param request request body
     * @return created product
     */
    ProductResponse create(CreateProductRequest request);

    /**
     * Retrieves the documents of a product.
     *
     * @param productId product identifier
     * @return document list
     */
    List<ProductDocumentResponse> getDocuments(String productId);

    /**
     * Forwards an uploaded product document to the order service.
     *
     * @param productId product identifier
     * @param file      inbound file
     * @return created document
     */
    ProductDocumentResponse uploadDocument(String productId, MultipartFile file);

    /**
     * Downloads a product document.
     *
     * @param productId  product identifier
     * @param documentId document identifier
     * @return document bytes and content headers
     */
    ClientFileResponseWrapper downloadDocument(String productId, String documentId);
}

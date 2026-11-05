package com.sqa.oms.order.modules.product.service;

import com.sqa.oms.order.modules.product.model.ProductDocumentResponse;

import java.util.List;

/**
 * Builds the response of the single and list product document endpoints.
 */
public interface IProductDocumentResponseCreatorService {

    /**
     * Returns a single document uploaded for a product, with a freshly generated presigned URL.
     *
     * @param productEntityId  business key of the product
     * @param documentEntityId business key of the document
     * @return ProductDocumentResponse
     */
    ProductDocumentResponse create(String productEntityId, String documentEntityId);

    /**
     * Lists every document uploaded for a product, each with a freshly generated presigned URL.
     *
     * @param productEntityId business key of the product
     * @return the documents
     */
    List<ProductDocumentResponse> createList(String productEntityId);
}

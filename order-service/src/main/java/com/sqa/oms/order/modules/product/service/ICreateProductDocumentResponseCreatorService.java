package com.sqa.oms.order.modules.product.service;

import com.sqa.oms.order.modules.product.model.ProductDocumentResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Builds the response of the upload product document endpoint.
 */
public interface ICreateProductDocumentResponseCreatorService {

    /**
     * Uploads a documentation file for a product and returns its full representation.
     *
     * @param productEntityId business key of the product
     * @param file            uploaded file
     * @return ProductDocumentResponse
     */
    ProductDocumentResponse create(String productEntityId, MultipartFile file);
}

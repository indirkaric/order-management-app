package com.sqa.oms.order.service.product;

import org.springframework.web.multipart.MultipartFile;

/**
 * Creates product documents.
 */
public interface ICreateProductDocumentService {

    /**
     * Uploads a documentation file for a product, storing the bytes in the storage service and
     * the file metadata locally.
     *
     * @param productEntityId business key of the product
     * @param file            uploaded file
     * @return the business key of the created document
     */
    String upload(String productEntityId, MultipartFile file);
}

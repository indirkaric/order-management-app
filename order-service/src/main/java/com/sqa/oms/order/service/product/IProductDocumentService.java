package com.sqa.oms.order.service.product;

import com.sqa.oms.order.modules.product.dto.ProductDocumentDTO;
import com.sqa.oms.order.modules.product.model.ProductDocumentContent;

import java.util.List;

/**
 * Read access to product documents.
 */
public interface IProductDocumentService {

    /**
     * Returns a single document uploaded for a product.
     *
     * @param productEntityId  business key of the product
     * @param documentEntityId business key of the document
     * @return the document
     */
    ProductDocumentDTO getByEntityId(String productEntityId, String documentEntityId);

    /**
     * Lists every document uploaded for a product, oldest first.
     *
     * @param productEntityId business key of the product
     * @return the documents
     */
    List<ProductDocumentDTO> listByProductEntityId(String productEntityId);

    /**
     * Downloads the raw bytes of a previously uploaded document.
     *
     * @param productEntityId  business key of the product
     * @param documentEntityId business key of the document
     * @return the document content
     */
    ProductDocumentContent download(String productEntityId, String documentEntityId);
}

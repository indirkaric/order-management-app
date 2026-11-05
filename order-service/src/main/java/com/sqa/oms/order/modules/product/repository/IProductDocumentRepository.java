package com.sqa.oms.order.modules.product.repository;

import com.sqa.oms.order.entity.ProductDocument;

import java.util.List;
import java.util.Optional;

public interface IProductDocumentRepository {

    /**
     * Persists a document's metadata.
     *
     * @param document document to persist
     * @return the persisted document
     */
    ProductDocument save(ProductDocument document);

    /**
     * Lists every document uploaded for a product, oldest first.
     *
     * @param productId owning product id
     * @return the documents
     */
    List<ProductDocument> findByProductId(Long productId);

    /**
     * Looks a document up by its business key, restricted to the product it was uploaded for.
     *
     * @param entityId  business key of the document
     * @param productId owning product id
     * @return the document when it exists and belongs to the product
     */
    Optional<ProductDocument> findByEntityIdAndProductId(String entityId, Long productId);

    /**
     * Checks whether a business key is already taken.
     *
     * @param entityId business key
     * @return true when the key is taken
     */
    boolean existsByEntityId(String entityId);
}

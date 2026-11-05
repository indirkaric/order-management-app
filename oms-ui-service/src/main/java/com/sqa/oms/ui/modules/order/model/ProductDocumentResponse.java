package com.sqa.oms.ui.modules.order.model;

import java.time.Instant;

/**
 * DTO of a product documentation file, mirroring order-service's own response shape.
 *
 * @param id          document id
 * @param productId   owning product id
 * @param fileName    original file name
 * @param contentType MIME type of the file
 * @param sizeBytes   file size in bytes
 * @param s3Url       freshly generated presigned download URL
 * @param uploadedAt  upload timestamp
 */
public record ProductDocumentResponse(Long id,
                                      Long productId,
                                      String fileName,
                                      String contentType,
                                      Long sizeBytes,
                                      String s3Url,
                                      Instant uploadedAt) {
}

package com.sqa.oms.order.modules.product.model;

import java.time.Instant;

public record ProductDocumentResponse(String entityId,
                                      String productEntityId,
                                      String fileName,
                                      String contentType,
                                      Long sizeBytes,
                                      String s3Url,
                                      Instant uploadedAt) {
}

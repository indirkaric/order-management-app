package com.sqa.oms.order.modules.product.dto;

import lombok.Data;

import java.time.Instant;

/**
 * Service layer representation of a product document, returned by
 * {@code IProductDocumentService} to the response creator services. The presigned download URL is
 * deliberately not part of this DTO: it is generated fresh every time the response is built.
 */
@Data
public class ProductDocumentDTO {

    private String entityId;

    private String productEntityId;

    private String fileName;

    private String contentType;

    private Long sizeBytes;

    private String s3Key;

    private Instant uploadedAt;
}

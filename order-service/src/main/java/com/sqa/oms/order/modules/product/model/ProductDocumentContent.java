package com.sqa.oms.order.modules.product.model;

public record ProductDocumentContent(String fileName,
                                     String contentType,
                                     byte[] content) {
}

package com.sqa.oms.order.client.storage;

public record PresignedUrlRequest(String filePath, boolean upload) {
}

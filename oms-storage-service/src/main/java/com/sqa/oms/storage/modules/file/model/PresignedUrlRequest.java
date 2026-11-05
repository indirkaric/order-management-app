package com.sqa.oms.storage.modules.file.model;

import jakarta.validation.constraints.NotBlank;

public record PresignedUrlRequest(@NotBlank String filePath, boolean upload) {
}

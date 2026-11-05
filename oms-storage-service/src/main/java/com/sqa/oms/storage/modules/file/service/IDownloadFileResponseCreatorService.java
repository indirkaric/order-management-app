package com.sqa.oms.storage.modules.file.service;

import org.springframework.core.io.InputStreamResource;

/**
 * Builds the response of the file download endpoint.
 */
public interface IDownloadFileResponseCreatorService {

    /**
     * Returns the raw bytes of a product documentation file.
     *
     * @param filePath the key of the file in the bucket
     * @return InputStreamResource of the file content, null when the object cannot be read
     */
    InputStreamResource create(String filePath);
}

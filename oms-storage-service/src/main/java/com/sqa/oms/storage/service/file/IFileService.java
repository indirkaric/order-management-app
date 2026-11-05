package com.sqa.oms.storage.service.file;

import org.springframework.core.io.InputStreamResource;

/**
 * Read access to a product documentation file's raw bytes.
 */
public interface IFileService {

    /**
     * Reads the raw bytes of a product documentation file.
     *
     * @param filePath the key of the file in the bucket
     * @return InputStreamResource of the file content, null when the object cannot be read
     */
    InputStreamResource downloadFile(String filePath);
}

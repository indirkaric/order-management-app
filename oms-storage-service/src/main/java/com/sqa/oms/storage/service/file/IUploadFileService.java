package com.sqa.oms.storage.service.file;

import org.springframework.web.multipart.MultipartFile;

/**
 * Stores the raw bytes of a product documentation file.
 */
public interface IUploadFileService {

    /**
     * Stores the raw bytes of a product documentation file.
     *
     * @param file     the file to store
     * @param filePath the target key in the bucket
     */
    void upload(MultipartFile file, String filePath);
}

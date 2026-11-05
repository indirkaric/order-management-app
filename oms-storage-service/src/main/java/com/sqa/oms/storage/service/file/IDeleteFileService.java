package com.sqa.oms.storage.service.file;

/**
 * Removes a product documentation file.
 */
public interface IDeleteFileService {

    /**
     * Removes a product documentation file.
     *
     * @param filePath the key of the file in the bucket
     */
    void delete(String filePath);
}

package com.sqa.oms.storage.service.storage;

import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

public interface ICloudStorageService {

    /**
     * Uploads a file to the cloud storage.
     *
     * @param file        the file to upload
     * @param externalUri the target key in the storage
     * @return String - message
     */
    String uploadFile(MultipartFile file, String externalUri);

    /**
     * Downloads a file from the cloud storage.
     *
     * @param fileName the key of the file in the storage
     * @return InputStreamResource - stream of the downloaded file, null when it cannot be read
     */
    InputStreamResource downloadFile(String fileName);

    /**
     * Deletes a file from the cloud storage.
     *
     * @param fileName the key of the file in the storage
     * @return boolean - true if file was deleted otherwise returns false
     */
    boolean deleteFile(String fileName);

    /**
     * Generates a pre-signed URL for a file in the cloud storage.
     *
     * @param fileName the key of the file in the storage
     * @param isUpload true for an upload (PUT) url, false for a download (GET) url
     * @return String - content url
     */
    String generatePreSignedURL(String fileName, boolean isUpload);
}

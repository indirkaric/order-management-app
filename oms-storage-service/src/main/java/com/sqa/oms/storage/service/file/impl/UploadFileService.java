package com.sqa.oms.storage.service.file.impl;

import com.sqa.oms.storage.service.file.IUploadFileService;
import com.sqa.oms.storage.service.storage.ICloudStorageService;
import com.sqa.oms.storage.util.Check;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Default implementation of the file upload operation.
 */
@Service
public final class UploadFileService implements IUploadFileService {

    private final ICloudStorageService cloudStorageService;

    /**
     * Constructor with dependency injection.
     *
     * @param cloudStorageService the cloud storage abstraction
     */
    public UploadFileService(final ICloudStorageService cloudStorageService) {
        Check.isNotNull(cloudStorageService, "cloudStorageService");

        this.cloudStorageService = cloudStorageService;
    }

    @Override
    public void upload(final MultipartFile file, final String filePath) {
        Check.isNotNull(file, "file");
        Check.isNotEmpty(filePath, "filePath");

        cloudStorageService.uploadFile(file, filePath);
    }
}

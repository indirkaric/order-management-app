package com.sqa.oms.storage.service.file.impl;

import com.sqa.oms.storage.service.file.IFileService;
import com.sqa.oms.storage.service.storage.ICloudStorageService;
import com.sqa.oms.storage.util.Check;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

/**
 * Default implementation of the read access to a product documentation file.
 */
@Service
public final class FileService implements IFileService {

    private final ICloudStorageService cloudStorageService;

    /**
     * Constructor with dependency injection.
     *
     * @param cloudStorageService the cloud storage abstraction
     */
    public FileService(final ICloudStorageService cloudStorageService) {
        Check.isNotNull(cloudStorageService, "cloudStorageService");

        this.cloudStorageService = cloudStorageService;
    }

    @Override
    public InputStreamResource downloadFile(final String filePath) {
        Check.isNotEmpty(filePath, "filePath");

        return cloudStorageService.downloadFile(filePath);
    }
}

package com.sqa.oms.storage.service.file.impl;

import com.sqa.oms.storage.service.file.IDeleteFileService;
import com.sqa.oms.storage.service.storage.ICloudStorageService;
import com.sqa.oms.storage.util.Check;
import org.springframework.stereotype.Service;

/**
 * Default implementation of the file delete operation.
 */
@Service
public final class DeleteFileService implements IDeleteFileService {

    private final ICloudStorageService cloudStorageService;

    /**
     * Constructor with dependency injection.
     *
     * @param cloudStorageService the cloud storage abstraction
     */
    public DeleteFileService(final ICloudStorageService cloudStorageService) {
        Check.isNotNull(cloudStorageService, "cloudStorageService");

        this.cloudStorageService = cloudStorageService;
    }

    @Override
    public void delete(final String filePath) {
        Check.isNotEmpty(filePath, "filePath");

        cloudStorageService.deleteFile(filePath);
    }
}

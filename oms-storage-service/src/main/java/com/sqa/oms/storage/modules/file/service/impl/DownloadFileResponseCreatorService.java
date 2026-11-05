package com.sqa.oms.storage.modules.file.service.impl;

import com.sqa.oms.storage.modules.file.service.IDownloadFileResponseCreatorService;
import com.sqa.oms.storage.service.file.IFileService;
import com.sqa.oms.storage.util.Check;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

/**
 * Default implementation of the file download response builder.
 */
@Service
public final class DownloadFileResponseCreatorService implements IDownloadFileResponseCreatorService {

    private final IFileService fileService;

    /**
     * Constructor with dependency injection.
     *
     * @param fileService core read access to a product documentation file
     */
    public DownloadFileResponseCreatorService(final IFileService fileService) {
        Check.isNotNull(fileService, "fileService");

        this.fileService = fileService;
    }

    @Override
    public InputStreamResource create(final String filePath) {
        Check.isNotEmpty(filePath, "filePath");

        return fileService.downloadFile(filePath);
    }
}

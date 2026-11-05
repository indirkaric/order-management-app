package com.sqa.oms.storage.service.file.impl;

import com.sqa.oms.storage.modules.file.model.PresignedUrlRequest;
import com.sqa.oms.storage.service.file.IGeneratePresignedUrlService;
import com.sqa.oms.storage.service.storage.ICloudStorageService;
import com.sqa.oms.storage.service.validator.IGeneratePresignedUrlServiceValidator;
import com.sqa.oms.storage.util.Check;
import org.springframework.stereotype.Service;

/**
 * Default implementation of the pre-signed URL generation.
 */
@Service
public final class GeneratePresignedUrlService implements IGeneratePresignedUrlService {

    private final IGeneratePresignedUrlServiceValidator generatePresignedUrlServiceValidator;

    private final ICloudStorageService cloudStorageService;

    /**
     * Constructor with dependency injection.
     *
     * @param generatePresignedUrlServiceValidator validates the pre-signed url request
     * @param cloudStorageService                  the cloud storage abstraction
     */
    public GeneratePresignedUrlService(final IGeneratePresignedUrlServiceValidator generatePresignedUrlServiceValidator,
                                       final ICloudStorageService cloudStorageService) {
        Check.isNotNull(generatePresignedUrlServiceValidator, "generatePresignedUrlServiceValidator");
        Check.isNotNull(cloudStorageService, "cloudStorageService");

        this.generatePresignedUrlServiceValidator = generatePresignedUrlServiceValidator;
        this.cloudStorageService = cloudStorageService;
    }

    @Override
    public String generate(final PresignedUrlRequest request) {
        Check.isNotNull(request, "request");
        Check.isNotEmpty(request.filePath(), "request.filePath");

        generatePresignedUrlServiceValidator.validate(request);

        return cloudStorageService.generatePreSignedURL(request.filePath(), request.upload());
    }
}

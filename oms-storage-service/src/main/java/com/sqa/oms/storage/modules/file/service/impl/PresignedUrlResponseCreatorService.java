package com.sqa.oms.storage.modules.file.service.impl;

import com.sqa.oms.storage.modules.file.model.PresignedUrlRequest;
import com.sqa.oms.storage.modules.file.model.PresignedUrlResponse;
import com.sqa.oms.storage.modules.file.service.IPresignedUrlResponseCreatorService;
import com.sqa.oms.storage.service.file.IGeneratePresignedUrlService;
import com.sqa.oms.storage.util.Check;
import org.springframework.stereotype.Service;

/**
 * Default implementation of the pre-signed URL response builder.
 */
@Service
public final class PresignedUrlResponseCreatorService implements IPresignedUrlResponseCreatorService {

    private final IGeneratePresignedUrlService generatePresignedUrlService;

    /**
     * Constructor with dependency injection.
     *
     * @param generatePresignedUrlService core pre-signed url generation
     */
    public PresignedUrlResponseCreatorService(final IGeneratePresignedUrlService generatePresignedUrlService) {
        Check.isNotNull(generatePresignedUrlService, "generatePresignedUrlService");

        this.generatePresignedUrlService = generatePresignedUrlService;
    }

    @Override
    public PresignedUrlResponse create(final PresignedUrlRequest request) {
        Check.isNotNull(request, "request");

        String url = generatePresignedUrlService.generate(request);

        return new PresignedUrlResponse(url);
    }
}

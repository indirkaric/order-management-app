package com.sqa.oms.storage.service.validator;

import com.sqa.oms.storage.modules.file.model.PresignedUrlRequest;

/**
 * Validates a pre-signed URL request.
 */
public interface IGeneratePresignedUrlServiceValidator {

    /**
     * Validates a pre-signed URL request.
     *
     * @param request the pre-signed url request
     */
    void validate(PresignedUrlRequest request);
}

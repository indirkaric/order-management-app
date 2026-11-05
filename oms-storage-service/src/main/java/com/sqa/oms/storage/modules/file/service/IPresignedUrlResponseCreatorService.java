package com.sqa.oms.storage.modules.file.service;

import com.sqa.oms.storage.modules.file.model.PresignedUrlRequest;
import com.sqa.oms.storage.modules.file.model.PresignedUrlResponse;

/**
 * Builds the response of the pre-signed URL endpoint.
 */
public interface IPresignedUrlResponseCreatorService {

    /**
     * Mints a pre-signed URL that lets the caller read or write a file directly.
     *
     * @param request the pre-signed url request
     * @return PresignedUrlResponse holding the generated url
     */
    PresignedUrlResponse create(PresignedUrlRequest request);
}

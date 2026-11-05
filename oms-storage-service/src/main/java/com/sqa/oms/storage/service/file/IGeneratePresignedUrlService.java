package com.sqa.oms.storage.service.file;

import com.sqa.oms.storage.modules.file.model.PresignedUrlRequest;

/**
 * Mints a pre-signed URL that lets the caller read or write a file directly.
 */
public interface IGeneratePresignedUrlService {

    /**
     * Mints a pre-signed URL that lets the caller read or write a file directly.
     *
     * @param request the pre-signed url request
     * @return the generated url
     */
    String generate(PresignedUrlRequest request);
}

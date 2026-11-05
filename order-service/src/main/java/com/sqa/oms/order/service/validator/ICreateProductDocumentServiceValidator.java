package com.sqa.oms.order.service.validator;

import org.springframework.web.multipart.MultipartFile;

/**
 * Validates the product document create flow.
 */
public interface ICreateProductDocumentServiceValidator {

    /**
     * Validates an uploaded document file.
     *
     * @param file file to validate
     */
    void validate(MultipartFile file);
}

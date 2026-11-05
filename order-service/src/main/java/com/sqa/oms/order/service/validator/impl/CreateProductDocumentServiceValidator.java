package com.sqa.oms.order.service.validator.impl;

import com.sqa.oms.order.exception.BusinessException;
import com.sqa.oms.order.service.validator.ICreateProductDocumentServiceValidator;
import com.sqa.oms.order.util.Check;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Default implementation of the product document create validation.
 */
@Service
public class CreateProductDocumentServiceValidator implements ICreateProductDocumentServiceValidator {

    @Override
    public void validate(final MultipartFile file) {
        Check.isNotNull(file, "file");

        if (file.isEmpty()) {
            throw new BusinessException("The uploaded file must not be empty.");
        }
    }
}

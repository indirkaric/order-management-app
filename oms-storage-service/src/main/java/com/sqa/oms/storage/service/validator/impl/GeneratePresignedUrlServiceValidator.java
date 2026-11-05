package com.sqa.oms.storage.service.validator.impl;

import com.sqa.oms.storage.constants.Constants;
import com.sqa.oms.storage.exception.BusinessException;
import com.sqa.oms.storage.modules.file.model.PresignedUrlRequest;
import com.sqa.oms.storage.service.validator.IGeneratePresignedUrlServiceValidator;
import com.sqa.oms.storage.util.Check;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Default implementation of the pre-signed URL request validator.
 */
@Service
public final class GeneratePresignedUrlServiceValidator implements IGeneratePresignedUrlServiceValidator {

    @Override
    public void validate(final PresignedUrlRequest request) {
        Check.isNotNull(request, "request");

        // A pre-signed PUT url is a write capability that escapes this service, so the endpoint
        // level read-or-write check is not enough: a read-only caller must never mint one.
        if (request.upload() && !hasAuthority(Constants.ApiScopes.PRODUCT_WRITE_SCOPE)) {
            throw new BusinessException("Upload pre-signed URL requires the "
                    + Constants.ApiScopes.PRODUCT_WRITE + " scope.");
        }
    }

    private boolean hasAuthority(final String authority) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }

        return authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority::equals);
    }
}

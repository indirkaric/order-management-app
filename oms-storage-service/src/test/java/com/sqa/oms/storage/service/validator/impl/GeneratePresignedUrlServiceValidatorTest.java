package com.sqa.oms.storage.service.validator.impl;

import com.sqa.oms.storage.constants.Constants;
import com.sqa.oms.storage.exception.BusinessException;
import com.sqa.oms.storage.modules.file.model.PresignedUrlRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GeneratePresignedUrlServiceValidatorTest {

    private static final String FILE_PATH = "products/42/manual.pdf";

    private GeneratePresignedUrlServiceValidator generatePresignedUrlServiceValidator;

    @BeforeEach
    void setUp() {
        generatePresignedUrlServiceValidator = new GeneratePresignedUrlServiceValidator();
    }

    /**
     * Clears the security context so that authorities do not leak between tests.
     */
    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAllowDownloadPresignedUrlForReadOnlyCaller() {
        authenticateWith(Constants.ApiScopes.PRODUCT_READ_SCOPE);

        assertThatCode(() -> generatePresignedUrlServiceValidator.validate(new PresignedUrlRequest(FILE_PATH, false)))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldAllowUploadPresignedUrlForWriteCaller() {
        authenticateWith(Constants.ApiScopes.PRODUCT_READ_SCOPE, Constants.ApiScopes.PRODUCT_WRITE_SCOPE);

        assertThatCode(() -> generatePresignedUrlServiceValidator.validate(new PresignedUrlRequest(FILE_PATH, true)))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectUploadPresignedUrlForReadOnlyCaller() {
        authenticateWith(Constants.ApiScopes.PRODUCT_READ_SCOPE);

        PresignedUrlRequest request = new PresignedUrlRequest(FILE_PATH, true);

        assertThatThrownBy(() -> generatePresignedUrlServiceValidator.validate(request))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getMessages()).isEqualTo(List.of("Upload pre-signed URL requires the "
                                + Constants.ApiScopes.PRODUCT_WRITE + " scope.")));
    }

    @Test
    void shouldRejectUploadPresignedUrlWhenThereIsNoAuthentication() {
        PresignedUrlRequest request = new PresignedUrlRequest(FILE_PATH, true);

        assertThatThrownBy(() -> generatePresignedUrlServiceValidator.validate(request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldRejectValidationWithoutRequest() {
        assertThatThrownBy(() -> generatePresignedUrlServiceValidator.validate(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private void authenticateWith(final String... authorities) {
        List<SimpleGrantedAuthority> grantedAuthorities = List.of(authorities).stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("caller", null, grantedAuthorities));
    }
}

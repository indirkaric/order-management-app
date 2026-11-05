package com.sqa.oms.order.service.validator.impl;

import com.sqa.oms.order.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateProductDocumentServiceValidatorTest {

    private final CreateProductDocumentServiceValidator validator = new CreateProductDocumentServiceValidator();

    @Test
    void shouldRejectEmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "spec.pdf", "application/pdf", new byte[0]);

        assertThatThrownBy(() -> validator.validate(emptyFile)).isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldAcceptNonEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "spec.pdf", "application/pdf", "content".getBytes());

        assertThatCode(() -> validator.validate(file)).doesNotThrowAnyException();
    }
}

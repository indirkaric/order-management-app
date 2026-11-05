package com.sqa.oms.storage.modules.file.service.impl;

import com.sqa.oms.storage.modules.file.model.PresignedUrlRequest;
import com.sqa.oms.storage.modules.file.model.PresignedUrlResponse;
import com.sqa.oms.storage.service.file.IGeneratePresignedUrlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PresignedUrlResponseCreatorServiceTest {

    private static final String FILE_PATH = "products/42/manual.pdf";

    @Mock
    private IGeneratePresignedUrlService generatePresignedUrlService;

    private PresignedUrlResponseCreatorService presignedUrlResponseCreatorService;

    @BeforeEach
    void setUp() {
        presignedUrlResponseCreatorService = new PresignedUrlResponseCreatorService(generatePresignedUrlService);
    }

    @Test
    void shouldWrapTheGeneratedUrlWhenCreatingTheResponse() {
        PresignedUrlRequest request = new PresignedUrlRequest(FILE_PATH, true);
        when(generatePresignedUrlService.generate(request)).thenReturn("http://put-url");

        PresignedUrlResponse response = presignedUrlResponseCreatorService.create(request);

        assertThat(response.url()).isEqualTo("http://put-url");
    }
}

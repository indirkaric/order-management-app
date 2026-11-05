package com.sqa.oms.storage.service.file.impl;

import com.sqa.oms.storage.exception.BusinessException;
import com.sqa.oms.storage.modules.file.model.PresignedUrlRequest;
import com.sqa.oms.storage.service.storage.ICloudStorageService;
import com.sqa.oms.storage.service.validator.IGeneratePresignedUrlServiceValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeneratePresignedUrlServiceTest {

    private static final String FILE_PATH = "products/42/manual.pdf";

    @Mock
    private IGeneratePresignedUrlServiceValidator generatePresignedUrlServiceValidator;

    @Mock
    private ICloudStorageService cloudStorageService;

    private GeneratePresignedUrlService generatePresignedUrlService;

    @BeforeEach
    void setUp() {
        generatePresignedUrlService =
                new GeneratePresignedUrlService(generatePresignedUrlServiceValidator, cloudStorageService);
    }

    @Test
    void shouldValidateThenDelegateToCloudStorage() {
        PresignedUrlRequest request = new PresignedUrlRequest(FILE_PATH, true);
        when(cloudStorageService.generatePreSignedURL(FILE_PATH, true)).thenReturn("http://put-url");

        String url = generatePresignedUrlService.generate(request);

        assertThat(url).isEqualTo("http://put-url");
        verify(generatePresignedUrlServiceValidator, times(1)).validate(request);
        verify(cloudStorageService, times(1)).generatePreSignedURL(FILE_PATH, true);
    }

    @Test
    void shouldNotCallCloudStorageWhenValidationFails() {
        PresignedUrlRequest request = new PresignedUrlRequest(FILE_PATH, true);
        doThrow(new BusinessException("not allowed")).when(generatePresignedUrlServiceValidator).validate(request);

        assertThatThrownBy(() -> generatePresignedUrlService.generate(request))
                .isInstanceOf(BusinessException.class);

        verify(cloudStorageService, never()).generatePreSignedURL(anyString(), anyBoolean());
    }

    @Test
    void shouldRejectGenerateWithoutRequest() {
        assertThatThrownBy(() -> generatePresignedUrlService.generate(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectGenerateWithoutFilePath() {
        PresignedUrlRequest request = new PresignedUrlRequest(null, false);

        assertThatThrownBy(() -> generatePresignedUrlService.generate(request))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

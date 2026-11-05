package com.sqa.oms.storage.service.file.impl;

import com.sqa.oms.storage.service.storage.ICloudStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UploadFileServiceTest {

    private static final String FILE_PATH = "products/42/manual.pdf";

    @Mock
    private ICloudStorageService cloudStorageService;

    private UploadFileService uploadFileService;

    @BeforeEach
    void setUp() {
        uploadFileService = new UploadFileService(cloudStorageService);
    }

    @Test
    void shouldDelegateUploadToCloudStorage() {
        MultipartFile file = new MockMultipartFile("file", "manual.pdf",
                "application/pdf", "content".getBytes(StandardCharsets.UTF_8));

        uploadFileService.upload(file, FILE_PATH);

        verify(cloudStorageService, times(1)).uploadFile(file, FILE_PATH);
    }

    @Test
    void shouldRejectUploadWithoutFile() {
        assertThatThrownBy(() -> uploadFileService.upload(null, FILE_PATH))
                .isInstanceOf(IllegalArgumentException.class);

        verify(cloudStorageService, never()).uploadFile(null, FILE_PATH);
    }

    @Test
    void shouldRejectUploadWithoutFilePath() {
        MultipartFile file = new MockMultipartFile("file", "manual.pdf",
                "application/pdf", "content".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> uploadFileService.upload(file, " "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

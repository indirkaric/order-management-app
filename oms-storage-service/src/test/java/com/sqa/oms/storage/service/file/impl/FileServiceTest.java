package com.sqa.oms.storage.service.file.impl;

import com.sqa.oms.storage.service.storage.ICloudStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    private static final String FILE_PATH = "products/42/manual.pdf";

    @Mock
    private ICloudStorageService cloudStorageService;

    private FileService fileService;

    @BeforeEach
    void setUp() {
        fileService = new FileService(cloudStorageService);
    }

    @Test
    void shouldDelegateDownloadToCloudStorage() {
        InputStreamResource expected = new InputStreamResource(
                new ByteArrayInputStream("content".getBytes(StandardCharsets.UTF_8)));

        when(cloudStorageService.downloadFile(FILE_PATH)).thenReturn(expected);

        assertThat(fileService.downloadFile(FILE_PATH)).isEqualTo(expected);
    }

    @Test
    void shouldReturnNullWhenCloudStorageCannotReadFile() {
        when(cloudStorageService.downloadFile(FILE_PATH)).thenReturn(null);

        assertThat(fileService.downloadFile(FILE_PATH)).isNull();
    }

    @Test
    void shouldRejectDownloadWithoutFilePath() {
        assertThatThrownBy(() -> fileService.downloadFile(" "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

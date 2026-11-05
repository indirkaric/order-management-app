package com.sqa.oms.storage.modules.file.service.impl;

import com.sqa.oms.storage.service.file.IFileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DownloadFileResponseCreatorServiceTest {

    private static final String FILE_PATH = "products/42/manual.pdf";

    @Mock
    private IFileService fileService;

    private DownloadFileResponseCreatorService downloadFileResponseCreatorService;

    @BeforeEach
    void setUp() {
        downloadFileResponseCreatorService = new DownloadFileResponseCreatorService(fileService);
    }

    @Test
    void shouldDelegateToTheServiceWhenCreatingTheDownloadResponse() {
        InputStreamResource expected = new InputStreamResource(
                new ByteArrayInputStream("content".getBytes(StandardCharsets.UTF_8)));

        when(fileService.downloadFile(FILE_PATH)).thenReturn(expected);

        assertThat(downloadFileResponseCreatorService.create(FILE_PATH)).isEqualTo(expected);
    }
}

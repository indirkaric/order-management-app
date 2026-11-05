package com.sqa.oms.storage.service.file.impl;

import com.sqa.oms.storage.service.storage.ICloudStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class DeleteFileServiceTest {

    private static final String FILE_PATH = "products/42/manual.pdf";

    @Mock
    private ICloudStorageService cloudStorageService;

    private DeleteFileService deleteFileService;

    @BeforeEach
    void setUp() {
        deleteFileService = new DeleteFileService(cloudStorageService);
    }

    @Test
    void shouldDelegateDeleteToCloudStorage() {
        deleteFileService.delete(FILE_PATH);

        verify(cloudStorageService, times(1)).deleteFile(FILE_PATH);
    }

    @Test
    void shouldRejectDeleteWithoutFilePath() {
        assertThatThrownBy(() -> deleteFileService.delete(null))
                .isInstanceOf(IllegalArgumentException.class);

        verify(cloudStorageService, never()).deleteFile(anyString());
    }
}

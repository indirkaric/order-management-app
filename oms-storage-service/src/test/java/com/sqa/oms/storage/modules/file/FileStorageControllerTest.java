package com.sqa.oms.storage.modules.file;

import com.sqa.oms.storage.modules.file.model.PresignedUrlRequest;
import com.sqa.oms.storage.modules.file.model.PresignedUrlResponse;
import com.sqa.oms.storage.modules.file.service.IDownloadFileResponseCreatorService;
import com.sqa.oms.storage.modules.file.service.IPresignedUrlResponseCreatorService;
import com.sqa.oms.storage.service.file.IDeleteFileService;
import com.sqa.oms.storage.service.file.IUploadFileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
public class FileStorageControllerTest {

    private static final String FILE_PATH = "products/42/manual.pdf";

    @Mock
    private IUploadFileService uploadFileService;

    @Mock
    private IDeleteFileService deleteFileService;

    @Mock
    private IDownloadFileResponseCreatorService downloadFileResponseCreatorService;

    @Mock
    private IPresignedUrlResponseCreatorService presignedUrlResponseCreatorService;

    private FileStorageController fileStorageController;

    /**
     * Creates the controller under test with its mocked dependencies.
     */
    @BeforeEach
    public void setUp() {
        fileStorageController = new FileStorageController(uploadFileService, deleteFileService,
                downloadFileResponseCreatorService, presignedUrlResponseCreatorService);
    }

    @Test
    public void shouldReturnNoContentAfterUpload() {

        MultipartFile file = new MockMultipartFile("file", "manual.pdf",
                "application/pdf", "content".getBytes(StandardCharsets.UTF_8));

        ResponseEntity<HttpStatus> response = fileStorageController.uploadFile(file, FILE_PATH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(uploadFileService, times(1)).upload(file, FILE_PATH);
    }

    @Test
    public void shouldStreamDownloadedFile() {

        InputStreamResource resource = new InputStreamResource(
                new ByteArrayInputStream("content".getBytes(StandardCharsets.UTF_8)));

        when(downloadFileResponseCreatorService.create(FILE_PATH)).thenReturn(resource);

        ResponseEntity<Resource> response = fileStorageController.downloadFile(FILE_PATH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM);
        assertThat(response.getBody()).isEqualTo(resource);
    }

    @Test
    public void shouldReturnNotFoundWhenFileIsMissing() {

        when(downloadFileResponseCreatorService.create(FILE_PATH)).thenReturn(null);

        assertThat(fileStorageController.downloadFile(FILE_PATH).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldReturnPresignedUrl() {

        PresignedUrlRequest request = new PresignedUrlRequest(FILE_PATH, true);

        when(presignedUrlResponseCreatorService.create(request))
                .thenReturn(new PresignedUrlResponse("http://put-url"));

        ResponseEntity<PresignedUrlResponse> response = fileStorageController.getPresignedUrl(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().url()).isEqualTo("http://put-url");
    }

    @Test
    public void shouldReturnNoContentAfterDelete() {

        ResponseEntity<HttpStatus> response = fileStorageController.deleteFile(FILE_PATH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(deleteFileService, times(1)).delete(FILE_PATH);
    }
}

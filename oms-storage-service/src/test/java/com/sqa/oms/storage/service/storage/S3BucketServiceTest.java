package com.sqa.oms.storage.service.storage;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.sqa.oms.storage.service.storage.impl.S3BucketService;
import org.apache.http.client.methods.HttpGet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.io.InputStreamResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
public class S3BucketServiceTest {

    private static final String BUCKET = "oms-product-files";

    private static final String FILE_PATH = "products/42/manual pdf.pdf";

    @Mock
    private AmazonS3 s3Client;

    private S3BucketService s3BucketService;

    /**
     * Creates the service under test with the bucket name normally injected from configuration.
     */
    @BeforeEach
    public void setUp() {
        s3BucketService = new S3BucketService(s3Client);
        ReflectionTestUtils.setField(s3BucketService, "bucketName", BUCKET);
    }

    @Test
    public void shouldRejectNullClient() {
        assertThrows(IllegalArgumentException.class, () -> new S3BucketService(null));
    }

    @Test
    public void shouldUploadFileToConfiguredBucket() {

        MultipartFile file = new MockMultipartFile("file", "manual pdf.pdf",
                "application/pdf", "content".getBytes(StandardCharsets.UTF_8));

        String result = s3BucketService.uploadFile(file, "products/42/manual%20pdf.pdf");

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client, times(1)).putObject(captor.capture());

        assertEquals(BUCKET, captor.getValue().getBucketName());
        assertEquals(FILE_PATH, captor.getValue().getKey());
        assertTrue(result.startsWith("File uploaded: "));
    }

    @Test
    public void shouldReturnFailureMessageWhenUploadFails() {

        MultipartFile file = new MockMultipartFile("file", "manual.pdf",
                "application/pdf", "content".getBytes(StandardCharsets.UTF_8));

        doThrow(new AmazonServiceException("boom")).when(s3Client).putObject(any(PutObjectRequest.class));

        assertTrue(s3BucketService.uploadFile(file, "products/42/manual.pdf").startsWith("Failed to upload file"));
    }

    @Test
    public void shouldDownloadFileAsStream() {

        S3Object s3Object = new S3Object();
        s3Object.setObjectContent(new S3ObjectInputStream(
                new ByteArrayInputStream("content".getBytes(StandardCharsets.UTF_8)), new HttpGet()));

        when(s3Client.getObject(BUCKET, FILE_PATH)).thenReturn(s3Object);

        InputStreamResource resource = s3BucketService.downloadFile("products/42/manual%20pdf.pdf");

        assertNotNull(resource);
    }

    @Test
    public void shouldReturnNullWhenDownloadFails() {

        when(s3Client.getObject(BUCKET, FILE_PATH)).thenThrow(new AmazonServiceException("missing"));

        assertNull(s3BucketService.downloadFile(FILE_PATH));
    }

    @Test
    public void shouldDeleteFile() {

        assertTrue(s3BucketService.deleteFile(FILE_PATH));

        verify(s3Client, times(1)).deleteObject(BUCKET, FILE_PATH);
    }

    @Test
    public void shouldReturnFalseWhenDeleteFails() {

        doThrow(new AmazonServiceException("boom")).when(s3Client).deleteObject(BUCKET, FILE_PATH);

        assertFalse(s3BucketService.deleteFile(FILE_PATH));
    }

    @Test
    public void shouldGeneratePresignedPutUrlForUpload() throws Exception {

        when(s3Client.generatePresignedUrl(any(GeneratePresignedUrlRequest.class)))
                .thenReturn(new URL("http://aws.oms.local:4566/oms-product-files/key"));

        String url = s3BucketService.generatePreSignedURL(FILE_PATH, true);

        ArgumentCaptor<GeneratePresignedUrlRequest> captor =
                ArgumentCaptor.forClass(GeneratePresignedUrlRequest.class);
        verify(s3Client, times(1)).generatePresignedUrl(captor.capture());

        assertEquals(HttpMethod.PUT, captor.getValue().getMethod());
        assertEquals(BUCKET, captor.getValue().getBucketName());
        assertEquals("http://aws.oms.local:4566/oms-product-files/key", url);
    }

    @Test
    public void shouldGeneratePresignedGetUrlForDownload() throws Exception {

        when(s3Client.generatePresignedUrl(any(GeneratePresignedUrlRequest.class)))
                .thenReturn(new URL("http://aws.oms.local:4566/oms-product-files/key"));

        s3BucketService.generatePreSignedURL(FILE_PATH, false);

        ArgumentCaptor<GeneratePresignedUrlRequest> captor =
                ArgumentCaptor.forClass(GeneratePresignedUrlRequest.class);
        verify(s3Client, times(1)).generatePresignedUrl(captor.capture());

        assertEquals(HttpMethod.GET, captor.getValue().getMethod());
    }

    @Test
    public void shouldReturnFallbackMessageWhenPresignedUrlFails() {

        when(s3Client.generatePresignedUrl(any(GeneratePresignedUrlRequest.class)))
                .thenThrow(new AmazonServiceException("boom"));

        assertEquals("Failed to generate pre-signed URL for file",
                s3BucketService.generatePreSignedURL(FILE_PATH, true));
    }

    @Test
    public void shouldRejectBlankFileName() {
        assertThrows(IllegalArgumentException.class, () -> s3BucketService.deleteFile(" "));
    }
}

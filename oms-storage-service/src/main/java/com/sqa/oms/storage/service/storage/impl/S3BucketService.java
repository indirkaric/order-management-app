package com.sqa.oms.storage.service.storage.impl;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.sqa.oms.storage.service.storage.ICloudStorageService;
import com.sqa.oms.storage.util.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@ConditionalOnProperty(
        name = "application.cloud-provider",
        havingValue = "AWS"
)
public class S3BucketService implements ICloudStorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3BucketService.class);

    private final AmazonS3 s3Client;

    @Value("${application.bucket.name}")
    private String bucketName;

    /**
     * Constructor with dependency injection.
     *
     * @param s3Client the AWS S3 client
     */
    public S3BucketService(final AmazonS3 s3Client) {

        Check.isNotNull(s3Client, "s3Client");

        this.s3Client = s3Client;
    }

    /**
     * Uploads file to S3 bucket.
     *
     * @param file        the file to upload
     * @param externalUri the target key in the bucket
     * @return String - message
     */
    @Override
    public String uploadFile(final MultipartFile file, final String externalUri) {

        Check.isNotNull(file, "file");
        Check.isNotEmpty(externalUri, "externalUri");

        return uploadDirectlyToS3(file, externalUri);
    }

    /**
     * Downloads file from S3 storage.
     *
     * @param fileName the full path (key) of the file in S3
     * @return InputStreamResource of the file content, null when the object cannot be read
     */
    @Override
    public InputStreamResource downloadFile(final String fileName) {

        Check.isNotEmpty(fileName, "fileName");

        String finalFileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);

        try {
            S3Object s3Object = s3Client.getObject(bucketName, finalFileName);
            return new InputStreamResource(s3Object.getObjectContent());
        } catch (Exception e) {
            LOGGER.error("Error while downloading file {}", finalFileName, e);
            return null;
        }
    }

    /**
     * Deletes file from S3 storage.
     *
     * @param fileName the key of the file in the bucket
     * @return boolean - deletion result
     */
    @Override
    public boolean deleteFile(final String fileName) {

        Check.isNotEmpty(fileName, "fileName");

        try {
            s3Client.deleteObject(bucketName, fileName);
            LOGGER.info("{} removed ...", fileName);
            return true;
        } catch (Exception e) {
            LOGGER.error("File is not deleted or not found {}", e.getMessage());
            return false;
        }
    }

    /**
     * Generate a pre-signed URL for a file stored in S3.
     *
     * @param fileName the key of the file in the bucket
     * @param isUpload true for a PUT url, false for a GET url
     * @return The pre-signed URL as a String.
     */
    @Override
    public String generatePreSignedURL(final String fileName, final boolean isUpload) {

        Check.isNotEmpty(fileName, "fileName");

        try {
            HttpMethod method = isUpload ? HttpMethod.PUT : HttpMethod.GET;

            GeneratePresignedUrlRequest generatePresignedUrlRequest =
                    new GeneratePresignedUrlRequest(bucketName, fileName)
                            .withMethod(method);

            URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);

            return url.toString();

        } catch (AmazonServiceException e) {
            LOGGER.error("Error generating pre-signed URL for file: {}", e.getMessage());
            return "Failed to generate pre-signed URL for file";
        }
    }

    private String uploadDirectlyToS3(final MultipartFile file, final String externalUri) {

        File convertedFile = convertMultipartFileToFile(file);

        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    bucketName,
                    URLDecoder.decode(externalUri, StandardCharsets.UTF_8),
                    convertedFile);
            s3Client.putObject(putObjectRequest);
            return "File uploaded: " + externalUri;
        } catch (AmazonServiceException e) {
            LOGGER.error("Error uploading file to S3: {}", e.getMessage());
            return "Failed to upload file: " + e.getMessage();
        } finally {
            if (convertedFile.exists() && !convertedFile.delete()) {
                LOGGER.warn("Could not delete temporary file {}", convertedFile.getAbsolutePath());
            }
        }
    }

    private File convertMultipartFileToFile(final MultipartFile file) {

        try {
            Path tempFile = Files.createTempFile("s3-upload-", "-"
                    + sanitizeFileName(file.getOriginalFilename()));
            file.transferTo(tempFile.toFile());
            return tempFile.toFile();
        } catch (IOException e) {
            LOGGER.error("Error converting multipart file to file", e);
            throw new UncheckedIOException("Failed to prepare file for upload: " + file.getOriginalFilename(), e);
        }
    }

    private String sanitizeFileName(final String originalFilename) {

        if (originalFilename == null || originalFilename.isBlank()) {
            return "file";
        }

        return originalFilename.replaceAll("[\\\\/]", "_");
    }
}

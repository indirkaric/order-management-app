package com.sqa.oms.storage.modules.file;

import com.sqa.oms.storage.constants.Constants;
import com.sqa.oms.storage.exception.response.BusinessExceptionResponse;
import com.sqa.oms.storage.modules.file.model.PresignedUrlRequest;
import com.sqa.oms.storage.modules.file.model.PresignedUrlResponse;
import com.sqa.oms.storage.modules.file.service.IDownloadFileResponseCreatorService;
import com.sqa.oms.storage.modules.file.service.IPresignedUrlResponseCreatorService;
import com.sqa.oms.storage.service.file.IDeleteFileService;
import com.sqa.oms.storage.service.file.IUploadFileService;
import com.sqa.oms.storage.util.Check;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = {"/api/v1/files"}, produces = APPLICATION_JSON_VALUE)
@Tag(
        name = "FileStorageController",
        description = "Provides endpoints for storing, retrieving and deleting product documentation files."
)
public class FileStorageController {

    private final IUploadFileService uploadFileService;

    private final IDeleteFileService deleteFileService;

    private final IDownloadFileResponseCreatorService downloadFileResponseCreatorService;

    private final IPresignedUrlResponseCreatorService presignedUrlResponseCreatorService;

    /**
     * Constructor with dependency injection.
     *
     * @param uploadFileService                   core file upload operation
     * @param deleteFileService                   core file delete operation
     * @param downloadFileResponseCreatorService  builds the file download response
     * @param presignedUrlResponseCreatorService  builds the pre-signed url response
     */
    public FileStorageController(final IUploadFileService uploadFileService,
                                 final IDeleteFileService deleteFileService,
                                 final IDownloadFileResponseCreatorService downloadFileResponseCreatorService,
                                 final IPresignedUrlResponseCreatorService presignedUrlResponseCreatorService) {
        Check.isNotNull(uploadFileService, "uploadFileService");
        Check.isNotNull(deleteFileService, "deleteFileService");
        Check.isNotNull(downloadFileResponseCreatorService, "downloadFileResponseCreatorService");
        Check.isNotNull(presignedUrlResponseCreatorService, "presignedUrlResponseCreatorService");

        this.uploadFileService = uploadFileService;
        this.deleteFileService = deleteFileService;
        this.downloadFileResponseCreatorService = downloadFileResponseCreatorService;
        this.presignedUrlResponseCreatorService = presignedUrlResponseCreatorService;
    }

    /**
     * This method handles HTTP POST requests to upload a file to S3.
     *
     * @param file     the file to upload
     * @param filePath target file path in S3
     * @return ResponseEntity&lt;HttpStatus&gt;
     */
    @Operation(
            summary = "Upload file to S3 bucket",
            description = "Upload file to S3 bucket.",
            security = {@SecurityRequirement(name = Constants.BEARER_KEY_VALUE)}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Successfully uploaded file.",
                    content = @Content
            )
    })
    @PostMapping(value = "/file", consumes = "multipart/form-data")
    @PreAuthorize(Constants.Authority.PRODUCT_WRITE_AUTHORITY)
    public ResponseEntity<HttpStatus> uploadFile(
            @Parameter(description = "The file to upload", required = true,
                    content = @Content(mediaType = "multipart/form-data",
                            schema = @Schema(type = "string", format = "binary")))
            @RequestParam("file") final MultipartFile file,

            @Parameter(description = "Target path in the S3 bucket", required = true)
            @RequestParam("filePath") final String filePath) {
        Check.isNotNull(file, "file");
        Check.isNotEmpty(filePath, "filePath");

        uploadFileService.upload(file, filePath);

        return ResponseEntity.noContent().build();
    }

    /**
     * This method handles HTTP GET requests to download a file from S3.
     *
     * @param filePath the full file path in storage
     * @return ResponseEntity&lt;Resource&gt;
     */
    @Operation(
            summary = "Download file from S3 bucket",
            description = "Downloads file from S3 bucket.",
            security = {@SecurityRequirement(name = Constants.BEARER_KEY_VALUE)}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully downloaded file.",
                    content = @Content
            )
    })
    @GetMapping(
            value = "/file/download",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @PreAuthorize(Constants.Authority.PRODUCT_READ_AUTHORITY)
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "Target file path in the S3 bucket", required = true)
            @RequestParam("filePath") final String filePath) {
        Check.isNotEmpty(filePath, "filePath");

        Resource fileResource = downloadFileResponseCreatorService.create(filePath);

        if (fileResource == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fileResource);
    }

    /**
     * This method handles HTTP POST requests to generate a pre-signed URL.
     *
     * @param request the pre-signed url request
     * @return ResponseEntity&lt;PresignedUrlResponse&gt;
     */
    @Operation(summary = "Get presigned url for a file",
            description = "Returns response body with URL",
            security = {@SecurityRequirement(name = Constants.BEARER_KEY_VALUE)})
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Presigned URL.",
                            content = {@Content(mediaType = APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = PresignedUrlResponse.class))}
                    ),
                    @ApiResponse(
                            responseCode = "422",
                            description = "Invalid input - field error",
                            content = {@Content(mediaType = APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = BusinessExceptionResponse.class))})})
    @PostMapping("/presigned-url")
    @PreAuthorize(Constants.Authority.PRODUCT_RESTRICTED_AUTHORITIES)
    public ResponseEntity<PresignedUrlResponse> getPresignedUrl(
            @Valid @RequestBody final PresignedUrlRequest request) {
        Check.isNotNull(request, "request");

        return ResponseEntity.ok(presignedUrlResponseCreatorService.create(request));
    }

    /**
     * This method handles HTTP DELETE requests to delete a file from S3.
     *
     * @param filePath the full file path in storage
     * @return ResponseEntity&lt;HttpStatus&gt;
     */
    @Operation(
            summary = "Delete file from S3 bucket",
            description = "Delete file from S3 bucket.",
            security = {@SecurityRequirement(name = Constants.BEARER_KEY_VALUE)}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Successfully deleted file.",
                    content = @Content
            )
    })
    @DeleteMapping(value = "/file")
    @PreAuthorize(Constants.Authority.PRODUCT_WRITE_AUTHORITY)
    public ResponseEntity<HttpStatus> deleteFile(
            @Parameter(description = "Target file path in the S3 bucket", required = true)
            @RequestParam("filePath") final String filePath) {
        Check.isNotEmpty(filePath, "filePath");

        deleteFileService.delete(filePath);

        return ResponseEntity.noContent().build();
    }

}

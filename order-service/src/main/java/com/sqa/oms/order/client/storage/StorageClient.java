package com.sqa.oms.order.client.storage;

import com.sqa.oms.order.client.BaseClient;
import com.sqa.oms.order.constants.Constants;
import com.sqa.oms.order.exception.BusinessException;
import com.sqa.oms.order.util.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class StorageClient extends BaseClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageClient.class);

    private static final String FILE_PATH_PARAM = "?filePath=";

    private final WebClient webClient;

    @Value("${client.storage.base-uri}")
    private String baseUri;

    /**
     * Constructor with dependency injection.
     *
     * @param webClient shared reactive http client
     */
    public StorageClient(final WebClient webClient) {
        Check.isNotNull(webClient, "webClient");
        this.webClient = webClient;
    }

    /**
     * Forwards the multipart bytes of an uploaded file to the storage service.
     *
     * @param file multipart file received by this service
     * @param key  storage key the file has to be stored under
     */
    public void uploadMultipart(final MultipartFile file, final String key) {
        Check.isNotNull(file, "file");
        Check.isNotEmpty(key, "key");

        String uri = baseUri + "files/file" + FILE_PATH_PARAM + encode(key);

        Resource fileResource = file.getResource();

        MultiValueMap<String, Object> multipartBody = new LinkedMultiValueMap<>();
        multipartBody.add("file", fileResource);

        try {
            webClient.post()
                    .uri(uri)
                    .header(Constants.SQA_CLIENT_ID_HEADER, Constants.ORDER_STORAGE_CLIENT)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(multipartBody))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception exception) {
            printErrorMessage(exception.getMessage());

            throw new BusinessException("Unable to store the document in the storage service.");
        }
    }

    /**
     * Asks the storage service for a presigned URL for the given key.
     *
     * @param key    storage key
     * @param upload true for a PUT style URL, false for a GET style URL
     * @return the presigned URL or null when the storage service did not return one
     */
    public String getPresignedUrl(final String key, final boolean upload) {
        Check.isNotEmpty(key, "key");

        String uri = baseUri + "files/presigned-url";

        try {
            PresignedUrlResponse response = webClient.post()
                    .uri(uri)
                    .header(Constants.SQA_CLIENT_ID_HEADER, Constants.ORDER_STORAGE_CLIENT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new PresignedUrlRequest(key, upload))
                    .retrieve()
                    .bodyToMono(PresignedUrlResponse.class)
                    .block();

            return response == null ? null : response.url();
        } catch (Exception exception) {
            LOGGER.warn("Unable to generate a presigned URL for key {}: {}", key, exception.getMessage());

            return null;
        }
    }

    /**
     * Downloads the raw bytes of a stored file.
     *
     * @param key storage key
     * @return the file content
     */
    public byte[] downloadFile(final String key) {
        Check.isNotEmpty(key, "key");

        String uri = baseUri + "files/file/download" + FILE_PATH_PARAM + encode(key);

        try {
            return webClient.get()
                    .uri(uri)
                    .header(Constants.SQA_CLIENT_ID_HEADER, Constants.ORDER_STORAGE_CLIENT)
                    .accept(MediaType.APPLICATION_OCTET_STREAM)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();
        } catch (Exception exception) {
            printErrorMessage(exception.getMessage());

            throw new BusinessException("Unable to download the document from the storage service.");
        }
    }

    private String encode(final String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}

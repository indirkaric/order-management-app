package com.sqa.oms.ui.client;

import com.sqa.oms.ui.client.response.ClientFileResponseWrapper;
import com.sqa.oms.ui.client.response.ClientResponseWrapper;
import com.sqa.oms.ui.constants.Constants;
import com.sqa.oms.ui.service.security.IScopeContext;
import com.sqa.oms.ui.util.Check;
import com.sqa.oms.ui.util.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;

/**
 * Common synchronous exchange and error suppression logic for all downstream clients.
 */
public abstract class BaseClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseClient.class);

    private final WebClient webClient;

    private final IScopeContext scopeContext;

    /**
     * Constructor with dependency injection.
     *
     * @param webClient    shared WebClient
     * @param scopeContext resolver of the authenticated caller, forwarded to every downstream call
     */
    protected BaseClient(final WebClient webClient, final IScopeContext scopeContext) {
        Check.isNotNull(webClient, "webClient");
        Check.isNotNull(scopeContext, "scopeContext");
        this.webClient = webClient;
        this.scopeContext = scopeContext;
    }

    /**
     * Converts a WebClient error into a wrapper so the caller can inspect the downstream status.
     *
     * @param ex error raised by WebClient
     * @return Mono with the downstream status and body
     */
    public static Mono<? extends ClientResponseWrapper> apply(final WebClientResponseException ex) {
        LOGGER.error("Error occurred with status: {}, message: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
        return Mono.just(new ClientResponseWrapper(
                HttpStatus.valueOf(ex.getStatusCode().value()), ex.getResponseBodyAsString()));
    }

    /**
     * Logs a failure of a synchronous call.
     *
     * @param message error message
     */
    public static void printErrorMessage(final String message) {
        LOGGER.error("Error occurred while making synchronous call: {}", message);
    }

    /**
     * Base URI of the downstream service, including the API version suffix.
     *
     * @return configured base URI
     */
    protected abstract String baseUri();

    /**
     * Value sent in the Sqa-Client-Id header on every call.
     *
     * @return client identifier
     */
    protected abstract String clientId();

    /**
     * Performs a GET call and forwards every inbound query parameter.
     *
     * @param path        path relative to the base URI
     * @param queryParams query parameters to forward, may be null
     * @return downstream status and body, or null when the service is unreachable
     */
    protected ClientResponseWrapper get(final String path, final MultiValueMap<String, String> queryParams) {
        Check.isNotEmpty(path, "path");
        return exchange(HttpMethod.GET, buildUri(path, queryParams), null);
    }

    /**
     * Performs a call with a JSON body.
     *
     * @param method HTTP method
     * @param path   path relative to the base URI
     * @param body   request body, may be null
     * @return downstream status and body, or null when the service is unreachable
     */
    protected ClientResponseWrapper send(final HttpMethod method, final String path, final Object body) {
        Check.isNotNull(method, "method");
        Check.isNotEmpty(path, "path");
        return exchange(method, buildUri(path, null), body);
    }

    /**
     * Re-wraps an inbound multipart file into a new multipart body and forwards it.
     *
     * @param path path relative to the base URI
     * @param file inbound file
     * @return downstream status and body, or null when the service is unreachable
     */
    protected ClientResponseWrapper sendMultipart(final String path, final MultipartFile file) {
        Check.isNotEmpty(path, "path");
        Check.isNotNull(file, "file");

        ClientResponseWrapper responseWrapper = null;

        try {
            MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
            bodyBuilder.part("file", toByteResource(file))
                    .filename(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename())
                    .header(HttpHeaders.CONTENT_TYPE, file.getContentType() == null
                            ? MediaType.APPLICATION_OCTET_STREAM_VALUE : file.getContentType());

            responseWrapper = webClient
                    .post()
                    .uri(buildUri(path, null))
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .accept(MediaType.APPLICATION_JSON)
                    .header(Constants.SQA_CLIENT_ID_HEADER, clientId())
                    .headers(this::forwardCallerIdentity)
                    .bodyValue(bodyBuilder.build())
                    .retrieve()
                    .toEntity(Object.class)
                    .map(responseEntity -> new ClientResponseWrapper(
                            HttpStatus.valueOf(responseEntity.getStatusCode().value()), responseEntity.getBody()))
                    .onErrorResume(WebClientResponseException.class, BaseClient::apply)
                    .block();
        } catch (Exception e) {
            printErrorMessage(e.getMessage());
        }

        return responseWrapper;
    }

    /**
     * Downloads a binary payload together with its content headers.
     *
     * @param path path relative to the base URI
     * @return downstream status, bytes and content headers, or null when the service is unreachable
     */
    protected ClientFileResponseWrapper download(final String path) {
        Check.isNotEmpty(path, "path");

        ClientFileResponseWrapper responseWrapper = null;

        try {
            // The whole file is buffered in memory before it is re-emitted; acceptable for
            // product documents but not for very large files, which would need reactive streaming.
            responseWrapper = webClient
                    .get()
                    .uri(buildUri(path, null))
                    .header(Constants.SQA_CLIENT_ID_HEADER, clientId())
                    .headers(this::forwardCallerIdentity)
                    .accept(MediaType.ALL)
                    .retrieve()
                    .toEntity(byte[].class)
                    .map(responseEntity -> new ClientFileResponseWrapper(
                            HttpStatus.valueOf(responseEntity.getStatusCode().value()),
                            responseEntity.getBody(),
                            responseEntity.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE),
                            responseEntity.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)))
                    .onErrorResume(WebClientResponseException.class, ex -> {
                        printErrorMessage(ex.getResponseBodyAsString());
                        return Mono.just(new ClientFileResponseWrapper(
                                HttpStatus.valueOf(ex.getStatusCode().value()), null, null, null));
                    })
                    .block();
        } catch (Exception e) {
            printErrorMessage(e.getMessage());
        }

        return responseWrapper;
    }

    private static ByteArrayResource toByteResource(final MultipartFile file) throws IOException {
        return new ByteArrayResource(file.getBytes());
    }

    // Downstream services apply customer scoped filtering based on this header instead of
    // validating the caller's JWT themselves, so it has to be set on every forwarded call.
    private void forwardCallerIdentity(final HttpHeaders headers) {
        String encodedUserInfo = scopeContext.getBase64EncodedUserInfo();

        if (!StringHelper.isEmpty(encodedUserInfo)) {
            headers.set(Constants.SQA_USER_ID_HEADER, encodedUserInfo);
        }
    }

    private ClientResponseWrapper exchange(final HttpMethod method, final URI uri, final Object body) {

        ClientResponseWrapper responseWrapper = null;

        try {
            WebClient.RequestBodySpec requestSpec = webClient
                    .method(method)
                    .uri(uri)
                    .header(Constants.SQA_CLIENT_ID_HEADER, clientId())
                    .headers(this::forwardCallerIdentity)
                    .accept(MediaType.APPLICATION_JSON);

            WebClient.RequestHeadersSpec<?> headersSpec = body == null
                    ? requestSpec
                    : requestSpec.contentType(MediaType.APPLICATION_JSON).bodyValue(body);

            responseWrapper = headersSpec
                    .retrieve()
                    .toEntity(Object.class)
                    .map(responseEntity -> new ClientResponseWrapper(
                            HttpStatus.valueOf(responseEntity.getStatusCode().value()), responseEntity.getBody()))
                    .onErrorResume(WebClientResponseException.class, BaseClient::apply)
                    .block();
        } catch (Exception e) {
            printErrorMessage(e.getMessage());
        }

        return responseWrapper;
    }

    private URI buildUri(final String path, final MultiValueMap<String, String> queryParams) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUri() + path);

        if (queryParams != null && !queryParams.isEmpty()) {
            builder.queryParams(queryParams);
        }

        return builder.build().encode().toUri();
    }
}

package com.sqa.oms.ui.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sqa.oms.ui.client.response.ClientFileResponseWrapper;
import com.sqa.oms.ui.client.response.ClientResponseWrapper;
import com.sqa.oms.ui.exception.BusinessException;
import com.sqa.oms.ui.exception.DownstreamClientException;
import com.sqa.oms.ui.exception.EntityNotFoundException;
import com.sqa.oms.ui.exception.ServiceUnavailableException;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Translates a downstream response into either a mapped payload or the matching exception.
 */
public final class ResponseUtil {

    // Downstream responses are now mapped onto concrete DTOs rather than JsonNode, so unknown
    // fields (e.g. the extra Spring Data Page metadata this gateway does not model) must be
    // ignored, and java.time types (Instant) need the JSR-310 module this plain ObjectMapper
    // does not get auto-registered with, unlike the Spring Boot managed one.
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .findAndRegisterModules();

    private static final String SERVICE_UNAVAILABLE_MESSAGE = "Service is unavailable. Please try again later.";

    private static final String NAMED_SERVICE_UNAVAILABLE_MESSAGE =
            "%s service is unavailable. Please try again later.";

    private static final String GENERIC_ERROR_MESSAGE = "Something went wrong";

    private static final String NOT_FOUND_MESSAGE = "Requested resource is not found.";

    private static final String MESSAGE_FIELD = "message";

    private static final String MESSAGES_FIELD = "messages";

    private ResponseUtil() {
    }

    /**
     * Ensures the downstream service responded.
     *
     * @param clientResponseWrapper client response, null when the service could not be reached
     * @param serviceName           name of the downstream service used in the error message
     * @return the same wrapper
     */
    public static ClientResponseWrapper ensureAvailable(final ClientResponseWrapper clientResponseWrapper,
                                                        final String serviceName) {
        if (clientResponseWrapper == null
                || clientResponseWrapper.getStatusCode() == null
                || clientResponseWrapper.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
            throw new ServiceUnavailableException(unavailableMessage(serviceName));
        }

        return clientResponseWrapper;
    }

    /**
     * Maps a downstream response to the given type, or raises the exception matching its status.
     *
     * @param clientResponseWrapper client response, null when the service could not be reached
     * @param responseType          type to map the response body to
     * @param serviceName           name of the downstream service used in the error messages
     * @param <T>                   response type
     * @return mapped response, or null when the downstream service returned no content
     * @throws Exception when the downstream service failed with a server error
     */
    public static <T> T handleResponse(final ClientResponseWrapper clientResponseWrapper,
                                       final Class<T> responseType,
                                       final String serviceName) throws Exception {

        HttpStatus status = ensureAvailable(clientResponseWrapper, serviceName).getStatusCode();
        Object body = clientResponseWrapper.getResponseBody();

        if (status == HttpStatus.NO_CONTENT) {
            return null;
        }

        if (status == HttpStatus.NOT_FOUND) {
            throw new EntityNotFoundException(readMessage(body, NOT_FOUND_MESSAGE));
        }

        if (status == HttpStatus.UNPROCESSABLE_ENTITY) {
            throw new BusinessException(readMessages(body));
        }

        if (status.is4xxClientError()) {
            throw new DownstreamClientException(status, body);
        }

        if (status.is5xxServerError()) {
            throw new Exception(GENERIC_ERROR_MESSAGE);
        }

        return OBJECT_MAPPER.convertValue(body, responseType);
    }

    /**
     * Validates a downstream binary response before its bytes are re-emitted to the caller.
     *
     * @param fileResponseWrapper downstream binary response
     * @param serviceName         name of the downstream service used in the error messages
     * @return the same wrapper
     * @throws Exception when the downstream service failed with a server error
     */
    public static ClientFileResponseWrapper handleFileResponse(final ClientFileResponseWrapper fileResponseWrapper,
                                                               final String serviceName) throws Exception {

        if (fileResponseWrapper == null
                || fileResponseWrapper.getStatusCode() == null
                || fileResponseWrapper.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
            throw new ServiceUnavailableException(unavailableMessage(serviceName));
        }

        HttpStatus status = fileResponseWrapper.getStatusCode();

        if (status == HttpStatus.NOT_FOUND) {
            throw new EntityNotFoundException(NOT_FOUND_MESSAGE);
        }

        if (status.is4xxClientError()) {
            throw new DownstreamClientException(status, null);
        }

        if (status.is5xxServerError()) {
            throw new Exception(GENERIC_ERROR_MESSAGE);
        }

        return fileResponseWrapper;
    }

    private static String unavailableMessage(final String serviceName) {
        return StringHelper.isEmpty(serviceName)
                ? SERVICE_UNAVAILABLE_MESSAGE
                : String.format(NAMED_SERVICE_UNAVAILABLE_MESSAGE, serviceName);
    }

    private static JsonNode toJsonNode(final Object body) {
        if (body == null) {
            return null;
        }

        try {
            return body instanceof String stringBody
                    ? OBJECT_MAPPER.readTree(stringBody)
                    : OBJECT_MAPPER.valueToTree(body);
        } catch (Exception e) {
            return null;
        }
    }

    private static String readMessage(final Object body, final String fallback) {
        JsonNode root = toJsonNode(body);

        if (root != null && root.hasNonNull(MESSAGE_FIELD)) {
            return root.get(MESSAGE_FIELD).asText();
        }

        return fallback;
    }

    private static List<String> readMessages(final Object body) {
        JsonNode root = toJsonNode(body);
        List<String> messages = new ArrayList<>();

        if (root != null && root.has(MESSAGES_FIELD) && root.get(MESSAGES_FIELD).isArray()) {
            root.get(MESSAGES_FIELD).forEach(message -> messages.add(message.asText()));
        } else if (root != null && root.hasNonNull(MESSAGE_FIELD)) {
            messages.add(root.get(MESSAGE_FIELD).asText());
        } else {
            messages.add(GENERIC_ERROR_MESSAGE);
        }

        return messages;
    }
}

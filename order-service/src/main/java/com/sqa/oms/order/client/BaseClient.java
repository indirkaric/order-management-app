package com.sqa.oms.order.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class BaseClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseClient.class);

    /**
     * Logs a failure of a synchronous outbound call.
     *
     * @param message message to log
     */
    public static void printErrorMessage(final String message) {
        LOGGER.error("Error occurred while making synchronous call: {}", message);
    }

    /**
     * Default set of downstream outages treated as ignorable: 404 / 500 / 503.
     *
     * @param <T> response body type
     * @return Function usable in onErrorResume(WebClientResponseException.class, ...)
     */
    public static <T> Function<WebClientResponseException, Mono<T>> suppressKnownErrors() {
        return suppressKnownErrors(HttpStatus.NOT_FOUND, HttpStatus.INTERNAL_SERVER_ERROR,
                HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Shared onErrorResume handler for WebClient calls: swallows the given statuses
     * as an empty result while logging a warning, and propagates every other error unchanged.
     *
     * @param statusesToSuppress statuses that should resolve to Mono.empty() instead of erroring
     * @param <T>                response body type
     * @return Function usable in onErrorResume(WebClientResponseException.class, ...)
     */
    public static <T> Function<WebClientResponseException, Mono<T>> suppressKnownErrors(
            final HttpStatus... statusesToSuppress) {

        Set<Integer> suppressed = Arrays.stream(statusesToSuppress)
                .map(HttpStatus::value)
                .collect(Collectors.toSet());

        return exception -> {
            int statusCode = exception.getStatusCode().value();

            if (suppressed.contains(statusCode)) {
                LOGGER.warn("Suppressing known downstream error: status={}, body={}",
                        statusCode, exception.getResponseBodyAsString());

                return Mono.empty();
            }

            return Mono.error(exception);
        };
    }
}

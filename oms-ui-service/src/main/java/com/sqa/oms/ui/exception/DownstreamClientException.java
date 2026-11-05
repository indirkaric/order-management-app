package com.sqa.oms.ui.exception;

import com.sqa.oms.ui.util.Check;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Exception used to propagate a downstream client error (4xx) transparently to the caller.
 */
@Getter
public class DownstreamClientException extends RuntimeException {

    private final HttpStatus statusCode;

    private final Object responseBody;

    /**
     * Constructor to set the downstream status and body.
     *
     * @param statusCode   status returned by the downstream service
     * @param responseBody body returned by the downstream service
     */
    public DownstreamClientException(final HttpStatus statusCode, final Object responseBody) {
        Check.isNotNull(statusCode, "statusCode");
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }
}

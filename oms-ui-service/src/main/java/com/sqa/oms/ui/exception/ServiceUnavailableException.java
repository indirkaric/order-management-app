package com.sqa.oms.ui.exception;

import com.sqa.oms.ui.util.Check;
import lombok.Getter;

/**
 * Exception thrown when a downstream service cannot be reached.
 */
@Getter
public class ServiceUnavailableException extends RuntimeException {

    private final String message;

    /**
     * Constructor to set the exception message.
     *
     * @param message string message
     */
    public ServiceUnavailableException(final String message) {
        Check.isNotEmpty(message, "message");
        this.message = message;
    }
}

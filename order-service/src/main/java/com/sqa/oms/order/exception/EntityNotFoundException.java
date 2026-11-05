package com.sqa.oms.order.exception;

import com.sqa.oms.order.util.Check;
import lombok.Getter;

/**
 * Exception for no data found errors.
 */
@Getter
public class EntityNotFoundException extends RuntimeException {

    private final String message;

    /**
     * Constructor to set the exception message.
     *
     * @param message string message
     */
    public EntityNotFoundException(final String message) {
        Check.isNotNull(message, "message");
        this.message = message;
    }
}

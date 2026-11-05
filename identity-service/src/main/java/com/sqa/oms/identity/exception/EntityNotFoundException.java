package com.sqa.oms.identity.exception;

import com.sqa.oms.identity.util.Check;
import lombok.Getter;

/**
 * Exception for no data found errors.
 */
@Getter
public class EntityNotFoundException extends RuntimeException {

    private final String message;

    /**
     * Creates the exception with the given message.
     *
     * @param message description of the entity that could not be found
     */
    public EntityNotFoundException(final String message) {
        Check.isNotNull(message, "message");
        this.message = message;
    }
}

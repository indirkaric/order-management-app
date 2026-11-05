package com.sqa.oms.identity.exception;

import com.sqa.oms.identity.util.Check;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Exception for business rules.
 */
@Getter
public final class BusinessException extends RuntimeException {

    private List<String> messages = new ArrayList<>();

    private String message;

    /**
     * Creates the exception from a single message.
     *
     * @param message the business rule violation message
     */
    public BusinessException(final String message) {
        Check.isNotEmpty(message, "message");
        this.message = message;
        messages.add(message);
    }

    /**
     * Creates the exception from a list of messages.
     *
     * @param messages the business rule violation messages
     */
    public BusinessException(final List<String> messages) {
        Check.isNotNull(messages, "messages");
        this.messages = messages;
    }
}

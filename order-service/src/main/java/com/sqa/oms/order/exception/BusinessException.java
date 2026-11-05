package com.sqa.oms.order.exception;

import com.sqa.oms.order.util.Check;
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
     * Constructor with a single message.
     *
     * @param message string message
     */
    public BusinessException(final String message) {
        Check.isNotEmpty(message, "message");
        this.message = message;
        messages.add(message);
    }

    /**
     * Constructor with several messages.
     *
     * @param messages list of the message strings
     */
    public BusinessException(final List<String> messages) {
        Check.isNotNull(messages, "messages");
        this.messages = messages;
    }
}

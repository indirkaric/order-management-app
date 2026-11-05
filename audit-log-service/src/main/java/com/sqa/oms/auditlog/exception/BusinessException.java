package com.sqa.oms.auditlog.exception;

import com.sqa.oms.auditlog.util.Check;
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
     * Constructor to set a single exception message.
     *
     * @param message String message
     */
    public BusinessException(final String message) {
        Check.isNotEmpty(message, "message");
        this.message = message;
        messages.add(message);
    }

    /**
     * Constructor to set a list of exception messages.
     *
     * @param messages List of the messages string
     */
    public BusinessException(final List<String> messages) {
        Check.isNotNull(messages, "messages");
        this.messages = messages;
    }
}

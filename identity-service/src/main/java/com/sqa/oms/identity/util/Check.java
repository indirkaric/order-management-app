package com.sqa.oms.identity.util;

import java.util.Collection;

/**
 * Static methods implementing the argument validations shared across the application.
 */
public final class Check {

    private static final String EMPTY = "";

    private Check() {
    }

    /**
     * Checks that a string is neither null nor blank.
     *
     * @param parameterValue value to validate
     * @param parameterName  name reported in the error message
     */
    public static void isNotEmpty(final String parameterValue, final String parameterName) {
        if (parameterValue == null) {
            throw new IllegalArgumentException(messageForNullValue(parameterName));
        }

        if (EMPTY.equals(parameterValue.trim())) {
            throw new IllegalArgumentException(messageForEmptyValue(parameterName));
        }
    }

    /**
     * Checks that a char array is neither null nor empty.
     *
     * @param parameterValue value to validate
     * @param parameterName  name reported in the error message
     */
    public static void isNotEmpty(final char[] parameterValue, final String parameterName) {
        if (parameterValue == null) {
            throw new IllegalArgumentException(messageForNullValue(parameterName));
        }

        if (parameterValue.length == 0) {
            throw new IllegalArgumentException(messageForEmptyValue(parameterName));
        }
    }

    /**
     * Checks that an object is not null.
     *
     * @param <T>             type of the validated object
     * @param parameterObject object to validate
     * @param parameterName   name reported in the error message
     */
    public static <T> void isNotNull(final T parameterObject, final String parameterName) {
        if (parameterObject == null) {
            throw new IllegalArgumentException(messageForNullValue(parameterName));
        }
    }

    /**
     * Checks that a collection is neither null nor empty.
     *
     * @param <T>             element type of the validated collection
     * @param parameterObject collection to validate
     * @param parameterName   name reported in the error message
     */
    public static <T> void isCollectionNotEmpty(final Collection<T> parameterObject, final String parameterName) {
        if (parameterObject == null) {
            throw new IllegalArgumentException(messageForNullValue(parameterName));
        }

        if (parameterObject.isEmpty()) {
            throw new IllegalArgumentException(messageForEmptyValue(parameterName));
        }
    }

    private static String messageForEmptyValue(final String parameterName) {
        return String.format("Parameter named %s is empty.", parameterName);
    }

    private static String messageForNullValue(final String parameterName) {
        return String.format("Parameter named %s is null.", parameterName);
    }
}

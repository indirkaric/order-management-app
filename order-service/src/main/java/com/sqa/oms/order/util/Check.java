package com.sqa.oms.order.util;

/**
 * Class with static methods implementing the common argument validations of the application.
 */
public final class Check {

    private static final String EMPTY = "";

    private Check() {
    }

    /**
     * Checks that a String is neither null nor blank.
     *
     * @param parameterValue value to verify
     * @param parameterName  name used in the error message
     */
    public static void isNotEmpty(final String parameterValue, final String parameterName) {
        if (parameterValue == null) {
            throw new IllegalArgumentException(generateMessageForNullValue(parameterName));
        } else if (EMPTY.equals(parameterValue.trim())) {
            throw new IllegalArgumentException(generateMessageForEmptyValue(parameterName));
        }
    }

    /**
     * Checks that a char array is neither null nor empty.
     *
     * @param parameterValue value to verify
     * @param parameterName  name used in the error message
     */
    public static void isNotEmpty(final char[] parameterValue, final String parameterName) {
        if (parameterValue == null) {
            throw new IllegalArgumentException(generateMessageForNullValue(parameterName));
        } else if (parameterValue.length == 0) {
            throw new IllegalArgumentException(generateMessageForEmptyValue(parameterName));
        }
    }

    /**
     * Checks that an object is not null.
     *
     * @param <T>             type of the verified object
     * @param parameterObject value to verify
     * @param parameterName   name used in the error message
     */
    public static <T> void isNotNull(final T parameterObject, final String parameterName) {
        if (parameterObject == null) {
            throw new IllegalArgumentException(generateMessageForNullValue(parameterName));
        }
    }

    private static String generateMessageForNullValue(final String parameterName) {
        return "Parameter " + parameterName + " must not be null.";
    }

    private static String generateMessageForEmptyValue(final String parameterName) {
        return "Parameter " + parameterName + " must not be empty.";
    }
}

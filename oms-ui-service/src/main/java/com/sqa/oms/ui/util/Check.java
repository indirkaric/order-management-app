package com.sqa.oms.ui.util;

/**
 * Class with static methods to implement several common validations among the application.
 */
public final class Check {

    public static final String EMPTY = "";

    private static final String NULL_VALUE_MESSAGE = "Parameter %s must not be null.";

    private static final String EMPTY_VALUE_MESSAGE = "Parameter %s must not be empty.";

    private Check() {
    }

    /**
     * Checks if a String is not empty nor null.
     *
     * @param parameterValue value to validate
     * @param parameterName  name used in the error message
     */
    public static void isNotEmpty(final String parameterValue, final String parameterName) {
        if (parameterValue == null) {
            throw new IllegalArgumentException(String.format(NULL_VALUE_MESSAGE, parameterName));
        } else if (EMPTY.equals(parameterValue.trim())) {
            throw new IllegalArgumentException(String.format(EMPTY_VALUE_MESSAGE, parameterName));
        }
    }

    /**
     * Check if an object is not null.
     *
     * @param <T>             type of the validated object
     * @param parameterObject object to validate
     * @param parameterName   name used in the error message
     */
    public static <T> void isNotNull(final T parameterObject, final String parameterName) {
        if (parameterObject == null) {
            throw new IllegalArgumentException(String.format(NULL_VALUE_MESSAGE, parameterName));
        }
    }
}

package com.sqa.oms.auditlog.util;

/**
 * Class with static methods to implement several common validations among the application.
 */
public final class Check {

    public static final String EMPTY = "";

    private Check() {
    }

    /**
     * Checks if a String is not empty nor null.
     *
     * @param parameterValue value to validate
     * @param parameterName  name of the validated parameter
     */
    public static void isNotEmpty(final String parameterValue, final String parameterName) {
        if (parameterValue == null) {
            throw new IllegalArgumentException(generateMessageForNullValue(parameterName));
        } else if (EMPTY.equals(parameterValue.trim())) {
            throw new IllegalArgumentException(generateMessageForEmptyValue(parameterName));
        }
    }

    /**
     * Check if an object is not null.
     *
     * @param <T>             type of the validated object
     * @param parameterObject object to validate
     * @param parameterName   name of the validated parameter
     */
    public static <T> void isNotNull(final T parameterObject, final String parameterName) {
        if (parameterObject == null) {
            throw new IllegalArgumentException(generateMessageForNullValue(parameterName));
        }
    }

    private static String generateMessageForEmptyValue(final String parameterName) {
        return String.format("Parameter named %s is empty.", parameterName);
    }

    private static String generateMessageForNullValue(final String parameterName) {
        return String.format("Parameter named %s is null.", parameterName);
    }
}

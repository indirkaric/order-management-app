package com.sqa.oms.identity.util;

/**
 * Static methods implementing the string guards used by the Criteria repositories.
 */
public final class StringHelper {

    private StringHelper() {
    }

    /**
     * Checks if a string is null or blank.
     *
     * @param value value to check
     * @return boolean
     */
    public static boolean isEmpty(final String value) {
        return value == null || value.trim().isEmpty();
    }
}

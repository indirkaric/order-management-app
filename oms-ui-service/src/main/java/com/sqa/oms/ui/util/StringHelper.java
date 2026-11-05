package com.sqa.oms.ui.util;

/**
 * Helper methods for String values.
 */
public final class StringHelper {

    private StringHelper() {
    }

    /**
     * Checks whether the given value is null or blank.
     *
     * @param value value to check
     * @return true when the value is null or blank
     */
    public static boolean isEmpty(final String value) {
        return value == null || value.trim().isEmpty();
    }
}

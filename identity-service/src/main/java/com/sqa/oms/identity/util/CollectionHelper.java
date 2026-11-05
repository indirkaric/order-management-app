package com.sqa.oms.identity.util;

import java.util.Collection;

/**
 * Static methods implementing the collection guards used by the Criteria repositories.
 */
public final class CollectionHelper {

    private CollectionHelper() {
    }

    /**
     * Checks if a collection is not null and has at least one element.
     *
     * @param collection collection to check
     * @return boolean
     */
    public static boolean isNotEmpty(final Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }
}

package com.sqa.oms.ui.util;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collection;

/**
 * Builds the query string forwarded to a downstream service from typed filters and paging,
 * so each client method only has to declare which fields it wants forwarded.
 */
public final class QueryParamsBuilder {

    private QueryParamsBuilder() {
    }

    /**
     * Starts a new, empty parameter map.
     *
     * @return an empty MultiValueMap
     */
    public static MultiValueMap<String, String> create() {
        return new LinkedMultiValueMap<>();
    }

    /**
     * Adds the page, size and sort parameters of a pageable.
     *
     * @param params   map to add to
     * @param pageable paging and sorting
     */
    public static void addPaging(final MultiValueMap<String, String> params, final Pageable pageable) {
        params.add("page", String.valueOf(pageable.getPageNumber()));
        params.add("size", String.valueOf(pageable.getPageSize()));

        for (Sort.Order order : pageable.getSort()) {
            params.add("sort", order.getProperty() + "," + order.getDirection());
        }
    }

    /**
     * Adds a parameter when its value is neither null nor blank.
     *
     * @param params map to add to
     * @param key    query parameter name
     * @param value  value to add, may be null or blank
     */
    public static void addIfPresent(final MultiValueMap<String, String> params, final String key,
                                    final String value) {
        if (!StringHelper.isEmpty(value)) {
            params.add(key, value.trim());
        }
    }

    /**
     * Adds a parameter when its value is not null, using its string representation.
     *
     * @param params map to add to
     * @param key    query parameter name
     * @param value  value to add, may be null
     */
    public static void addIfPresent(final MultiValueMap<String, String> params, final String key,
                                    final Object value) {
        if (value != null) {
            params.add(key, value.toString());
        }
    }

    /**
     * Adds one parameter per non blank element of a collection.
     *
     * @param params map to add to
     * @param key    query parameter name
     * @param values values to add, may be null
     */
    public static void addAllIfPresent(final MultiValueMap<String, String> params, final String key,
                                       final Collection<String> values) {
        if (values != null) {
            values.forEach(value -> addIfPresent(params, key, value));
        }
    }
}

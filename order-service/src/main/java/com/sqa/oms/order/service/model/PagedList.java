package com.sqa.oms.order.service.model;

import com.sqa.oms.order.util.Check;

import java.util.List;

/**
 * Service layer paging result, decoupled from the Spring Data {@code Page} used at the
 * repository layer.
 *
 * @param <T> type of the paged items
 */
public final class PagedList<T> {

    private final List<T> items;

    private final long count;

    /**
     * Constructor with dependency injection.
     *
     * @param items items of the current page
     * @param count total number of matching elements across all pages
     */
    public PagedList(final List<T> items, final long count) {
        Check.isNotNull(items, "items");

        this.items = items;
        this.count = count;
    }

    public List<T> getItems() {
        return this.items;
    }

    public long getCount() {
        return this.count;
    }
}

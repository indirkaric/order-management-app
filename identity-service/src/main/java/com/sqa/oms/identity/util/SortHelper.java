package com.sqa.oms.identity.util;

import com.sqa.oms.identity.constants.Constants;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Optional;

/**
 * Translates a Spring {@link Pageable}'s sort into a Criteria API {@link Order}.
 */
public final class SortHelper {

    private SortHelper() {
    }

    /**
     * Returns the first sort property of a pageable, mapping the {@code id} alias
     * back to {@code entityId} since the API never exposes the surrogate key.
     *
     * @param pageable paging and sorting
     * @return the sort property, or null when unsorted
     */
    public static String getSortProperty(final Pageable pageable) {
        Check.isNotNull(pageable, "pageable");

        if (pageable.getSort().isUnsorted()) {
            return null;
        }

        String property = Constants.ENTITY_ID;
        Optional<Sort.Order> first = pageable.getSort().get().findFirst();
        if (first.isPresent()) {
            property = first.get().getProperty();
        }

        if (property.equalsIgnoreCase("id")) {
            property = Constants.ENTITY_ID;
        }

        return property;
    }

    /**
     * Checks whether the first sort order of a pageable is descending.
     *
     * @param pageable paging and sorting
     * @return boolean
     */
    public static boolean isSortDescending(final Pageable pageable) {
        Check.isNotNull(pageable, "pageable");

        Optional<Sort.Order> first = pageable.getSort().get().findFirst();
        return first.map(order -> order.getDirection().equals(Sort.Direction.DESC)).orElse(false);
    }

    /**
     * Builds the Criteria {@link Order} matching a pageable's sort.
     *
     * @param criteriaBuilder criteria builder
     * @param root            query root
     * @param pageable        paging and sorting
     * @return the Criteria order, or null when unsorted
     */
    public static Order getOrdering(final CriteriaBuilder criteriaBuilder, final Root<?> root,
                                    final Pageable pageable) {
        String sortProperty = getSortProperty(pageable);
        if (sortProperty == null) {
            return null;
        }

        return isSortDescending(pageable)
                ? criteriaBuilder.desc(root.get(sortProperty))
                : criteriaBuilder.asc(root.get(sortProperty));
    }
}

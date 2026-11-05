package com.sqa.oms.order.util;

import com.sqa.oms.order.annotation.Lowercase;
import com.sqa.oms.order.annotation.Trimmed;
import com.sqa.oms.order.annotation.Uppercase;
import com.sqa.oms.order.annotation.UppercaseList;
import com.sqa.oms.order.exception.BusinessException;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Normalizes request fields annotated with {@link Trimmed}, {@link Uppercase}, {@link Lowercase}
 * or {@link UppercaseList}, so controllers no longer need ad hoc {@code trim()}/{@code
 * toUpperCase()} calls before handing the request off to the service layer.
 */
public final class CustomFieldProcessor {

    private CustomFieldProcessor() {
    }

    /**
     * Processes every annotated field of an object, applying the behaviour of its annotation.
     *
     * @param object the request object whose fields are normalized in place
     * @param <T>    type of the object
     */
    public static <T> void processAnnotatedFields(final T object) {
        Check.isNotNull(object, "object");

        processFields(object.getClass(), object);
    }

    private static void processFields(final Class<?> clazz, final Object object) {
        if (clazz == null || clazz == Object.class) {
            return;
        }

        try {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);

                if (field.isAnnotationPresent(Trimmed.class)) {
                    processTrimmedField(field, object);
                }

                if (field.isAnnotationPresent(Uppercase.class)) {
                    processUppercaseField(field, object);
                }

                if (field.isAnnotationPresent(Lowercase.class)) {
                    processLowercaseField(field, object);
                }

                if (field.isAnnotationPresent(UppercaseList.class)) {
                    processUppercaseListField(field, object);
                }
            }
        } catch (IllegalAccessException e) {
            throw new BusinessException("Error during field processing: " + e.getMessage());
        }

        processFields(clazz.getSuperclass(), object);
    }

    private static void processTrimmedField(final Field field, final Object object) throws IllegalAccessException {
        Object value = field.get(object);
        if (value instanceof String) {
            field.set(object, ((String) value).trim().replaceAll("\\s+", " "));
        }
    }

    private static void processUppercaseField(final Field field, final Object object) throws IllegalAccessException {
        Object value = field.get(object);
        if (value instanceof String) {
            field.set(object, ((String) value).toUpperCase(Locale.ROOT));
        }
    }

    private static void processLowercaseField(final Field field, final Object object) throws IllegalAccessException {
        Object value = field.get(object);
        if (value instanceof String) {
            field.set(object, ((String) value).toLowerCase(Locale.ROOT));
        }
    }

    @SuppressWarnings("unchecked")
    private static void processUppercaseListField(final Field field, final Object object)
            throws IllegalAccessException {
        Object value = field.get(object);
        if (value instanceof List) {
            List<String> uppercased = ((List<String>) value).stream()
                    .map(entry -> entry == null ? null : entry.trim().toUpperCase(Locale.ROOT))
                    .collect(Collectors.toList());
            field.set(object, uppercased);
        }
    }
}

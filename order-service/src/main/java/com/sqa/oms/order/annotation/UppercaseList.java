package com.sqa.oms.order.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@code List<String>} field, typically a list of business keys, whose elements are
 * trimmed and uppercased by {@code CustomFieldProcessor} before the request reaches the service
 * layer.
 */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface UppercaseList {

}

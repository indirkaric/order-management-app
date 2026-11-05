package com.sqa.oms.order.service.security;

import java.util.Set;

/**
 * Single abstraction the domain code uses to learn who is calling. It hides whether the identity
 * came from a validated Keycloak JWT or from the trusted API gateway headers.
 */
public interface ICallerContext {

    String getUserId();

    Set<String> getAuthorities();

    boolean isElevated();
}

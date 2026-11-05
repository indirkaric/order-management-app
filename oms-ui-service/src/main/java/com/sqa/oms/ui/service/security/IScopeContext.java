package com.sqa.oms.ui.service.security;

import java.util.List;

/**
 * Exposes the authenticated caller of the current request.
 */
public interface IScopeContext {

    /**
     * Returns the user_id claim of the validated JWT.
     *
     * @return user identifier, or null when there is no authenticated caller
     */
    String getCurrentUserId();

    /**
     * Returns the granted authority names of the current caller.
     *
     * @return authority names, never null
     */
    List<String> getCurrentAuthorities();

    /**
     * Returns the base64 encoded caller identity forwarded to the downstream services.
     *
     * @return base64 encoded user info, or null when there is no authenticated caller
     */
    String getBase64EncodedUserInfo();
}

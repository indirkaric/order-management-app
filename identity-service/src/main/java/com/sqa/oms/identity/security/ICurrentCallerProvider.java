package com.sqa.oms.identity.security;

/**
 * Resolves the identity of the caller of the current request.
 */
public interface ICurrentCallerProvider {

    /**
     * Returns the name used for audit columns and for the {@code performedBy} field of the events.
     *
     * @return the caller name, or the system actor when the request is not authenticated
     */
    String resolveActor();
}

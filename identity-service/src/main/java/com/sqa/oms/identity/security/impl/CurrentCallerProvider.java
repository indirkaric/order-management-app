package com.sqa.oms.identity.security.impl;

import com.sqa.oms.identity.constants.Constants;
import com.sqa.oms.identity.security.CallerIdentity;
import com.sqa.oms.identity.security.ICurrentCallerProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * Reads the caller identity out of the Spring Security context.
 */
@Component
public class CurrentCallerProvider implements ICurrentCallerProvider {

    @Override
    public final String resolveActor() {
        CallerIdentity forwarded = forwardedIdentity();
        if (forwarded != null && forwarded.userId() != null) {
            // On the gateway path, the authenticated principal is the service client id
            // (e.g. "Gateway.Identity"), never the end user - this is the only source of truth.
            return forwarded.userId();
        }

        Authentication authentication = currentAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return Constants.SYSTEM_ACTOR;
        }

        return authentication.getName();
    }

    private Authentication currentAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        return authentication;
    }

    private CallerIdentity forwardedIdentity() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return null;
        }

        Object attribute = requestAttributes
                .getAttribute(Constants.CALLER_IDENTITY_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);

        return attribute instanceof CallerIdentity callerIdentity ? callerIdentity : null;
    }
}

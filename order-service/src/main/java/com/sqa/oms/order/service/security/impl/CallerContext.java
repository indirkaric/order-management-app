package com.sqa.oms.order.service.security.impl;

import com.sqa.oms.order.constants.Constants;
import com.sqa.oms.order.service.security.CallerIdentity;
import com.sqa.oms.order.service.security.ICallerContext;
import com.sqa.oms.order.util.StringHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CallerContext implements ICallerContext {

    @Override
    public String getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            String userId = jwtAuthentication.getToken().getClaimAsString(Constants.USER_ID_CLAIM);

            if (!StringHelper.isEmpty(userId)) {
                return userId;
            }

            return jwtAuthentication.getToken().getClaimAsString(Constants.SUBJECT_CLAIM);
        }

        Optional<CallerIdentity> forwarded = forwardedIdentity();

        if (forwarded.isPresent()) {
            return forwarded.get().userId();
        }

        return authentication == null ? null : authentication.getName();
    }

    @Override
    public Set<String> getAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken) {
            return toAuthorityNames(authentication);
        }

        Optional<CallerIdentity> forwarded = forwardedIdentity();

        if (forwarded.isPresent()) {
            Set<String> authorities = forwarded.get().authorities();

            return authorities == null ? Collections.emptySet() : authorities;
        }

        return authentication == null ? Collections.emptySet() : toAuthorityNames(authentication);
    }

    @Override
    public boolean isElevated() {
        Set<String> authorities = getAuthorities();

        return Constants.Authority.elevatedScopes().stream().anyMatch(authorities::contains);
    }

    private Optional<CallerIdentity> forwardedIdentity() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        if (requestAttributes == null) {
            return Optional.empty();
        }

        Object attribute = requestAttributes
                .getAttribute(Constants.CALLER_IDENTITY_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);

        if (attribute instanceof CallerIdentity callerIdentity) {
            return Optional.of(callerIdentity);
        }

        return Optional.empty();
    }

    private Set<String> toAuthorityNames(final Authentication authentication) {
        return authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }
}

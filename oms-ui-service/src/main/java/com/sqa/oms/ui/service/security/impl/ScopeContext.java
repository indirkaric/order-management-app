package com.sqa.oms.ui.service.security.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sqa.oms.ui.constants.Constants;
import com.sqa.oms.ui.dto.UserInfoDTO;
import com.sqa.oms.ui.service.security.IScopeContext;
import com.sqa.oms.ui.util.StringHelper;
import lombok.SneakyThrows;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Reads the authenticated caller from the Spring Security context.
 */
@Service
public class ScopeContext implements IScopeContext {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return null;
        }

        return jwt.getClaimAsString(Constants.USER_ID_CLAIM);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getCurrentAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<String> authorities = new ArrayList<>();

        if (authentication == null) {
            return authorities;
        }

        for (GrantedAuthority grantedAuthority : authentication.getAuthorities()) {
            authorities.add(grantedAuthority.getAuthority());
        }

        return authorities;
    }

    /**
     * {@inheritDoc}
     */
    @SneakyThrows
    @Override
    public String getBase64EncodedUserInfo() {
        String userId = getCurrentUserId();
        List<String> authorities = getCurrentAuthorities();

        if (StringHelper.isEmpty(userId) && authorities.isEmpty()) {
            return null;
        }

        // Downstream services read userId and authorities from this header to apply customer-scoped
        // filtering, so they never have to validate the caller's JWT themselves.
        String json = OBJECT_MAPPER.writeValueAsString(new UserInfoDTO(userId, authorities));

        return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }
}

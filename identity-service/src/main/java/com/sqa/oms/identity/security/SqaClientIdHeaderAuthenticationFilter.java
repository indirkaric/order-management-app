package com.sqa.oms.identity.security;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sqa.oms.identity.constants.Constants;
import com.sqa.oms.identity.util.StringHelper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * Authenticates trusted service to service calls carrying the {@code Sqa-Client-Id} header.
 */
public class SqaClientIdHeaderAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqaClientIdHeaderAuthenticationFilter.class);

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    protected final void doFilterInternal(final HttpServletRequest request,
                                          final HttpServletResponse response,
                                          final FilterChain filterChain)
            throws ServletException, IOException {

        String sqaClientId = request.getHeader(Constants.SQA_CLIENT_ID_HEADER);
        if (sqaClientId != null) {
            List<SimpleGrantedAuthority> grantedAuthorityList = getAuthoritiesBasedOnClient(sqaClientId);

            final UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(sqaClientId, null, grantedAuthorityList);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // No real JWT exists on the gateway path; this is the only way to know which end user
            // actually triggered the write, for audit columns and the Kafka event's performedBy.
            CallerIdentity callerIdentity = decodeCallerIdentity(request.getHeader(Constants.SQA_USER_ID_HEADER));

            if (callerIdentity != null) {
                request.setAttribute(Constants.CALLER_IDENTITY_ATTRIBUTE, callerIdentity);
            }
        }

        filterChain.doFilter(request, response);
    }

    private List<SimpleGrantedAuthority> getAuthoritiesBasedOnClient(final String sqaClientId) {
        if (Constants.GATEWAY_IDENTITY_CLIENT.equals(sqaClientId)) {
            return Constants.Authority.fullAuthorities();
        }

        return List.of();
    }

    private CallerIdentity decodeCallerIdentity(final String encodedHeader) {
        if (StringHelper.isEmpty(encodedHeader)) {
            return null;
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(encodedHeader.trim());

            return objectMapper.readValue(new String(decoded, StandardCharsets.UTF_8), CallerIdentity.class);
        } catch (IllegalArgumentException | IOException exception) {
            LOGGER.warn("Unable to decode the {} header", Constants.SQA_USER_ID_HEADER, exception);

            return null;
        }
    }
}

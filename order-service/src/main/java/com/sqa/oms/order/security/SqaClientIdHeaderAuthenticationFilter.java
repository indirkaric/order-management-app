package com.sqa.oms.order.security;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sqa.oms.order.constants.Constants;
import com.sqa.oms.order.service.security.CallerIdentity;
import com.sqa.oms.order.util.StringHelper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

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

        if (Constants.GATEWAY_ORDER_CLIENT.equals(sqaClientId)) {
            // The gateway already validated the end user JWT and its permissions, so the service
            // trusts this header and grants the full set of scopes it is allowed to proxy.
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    sqaClientId, null, Constants.Authority.gatewayAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            CallerIdentity callerIdentity = decodeCallerIdentity(request.getHeader(Constants.SQA_USER_ID_HEADER));

            if (callerIdentity != null) {
                // No real JWT exists on the gateway path; the forwarded end user identity is what
                // the customer scoped filtering has to be based on.
                request.setAttribute(Constants.CALLER_IDENTITY_ATTRIBUTE, callerIdentity);
            }
        }

        filterChain.doFilter(request, response);
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

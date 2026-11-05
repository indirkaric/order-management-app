package com.sqa.oms.storage.security;

import com.sqa.oms.storage.constants.Constants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class SqaClientIdHeaderAuthenticationFilter extends OncePerRequestFilter {

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
        }

        filterChain.doFilter(request, response);
    }

    private List<SimpleGrantedAuthority> getAuthoritiesBasedOnClient(final String sqaClientId) {

        if (sqaClientId.equals(Constants.ORDER_STORAGE_CLIENT)) {
            return Constants.Authority.orderServiceAuthorities();
        }

        return new ArrayList<>();
    }
}

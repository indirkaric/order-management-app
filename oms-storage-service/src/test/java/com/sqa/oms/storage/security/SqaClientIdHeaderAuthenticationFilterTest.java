package com.sqa.oms.storage.security;

import com.sqa.oms.storage.constants.Constants;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class SqaClientIdHeaderAuthenticationFilterTest {

    private final SqaClientIdHeaderAuthenticationFilter filter = new SqaClientIdHeaderAuthenticationFilter();

    /**
     * Clears the security context so that authorities do not leak between tests.
     */
    @AfterEach
    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void shouldGrantProductScopesToOrderService() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(Constants.SQA_CLIENT_ID_HEADER, Constants.ORDER_STORAGE_CLIENT);

        filter.doFilter(request, new MockHttpServletResponse(), mock(FilterChain.class));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
        List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        assertEquals(2, authorities.size());
        assertTrue(authorities.contains(Constants.ApiScopes.PRODUCT_READ_SCOPE));
        assertTrue(authorities.contains(Constants.ApiScopes.PRODUCT_WRITE_SCOPE));
    }

    @Test
    public void shouldGrantNothingToUnknownClient() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(Constants.SQA_CLIENT_ID_HEADER, "Unknown.Client");

        filter.doFilter(request, new MockHttpServletResponse(), mock(FilterChain.class));

        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().isEmpty());
    }

    @Test
    public void shouldLeaveContextEmptyWithoutClientIdHeader() throws Exception {

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), mock(FilterChain.class));

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}

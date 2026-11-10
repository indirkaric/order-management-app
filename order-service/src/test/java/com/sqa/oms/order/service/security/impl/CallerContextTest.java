package com.sqa.oms.order.service.security.impl;

import com.sqa.oms.order.constants.Constants;
import com.sqa.oms.order.service.security.CallerIdentity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CallerContextTest {

    private static final long TOKEN_TTL_SECONDS = 60L;

    private static final String ELEVATED = Constants.Authority.elevatedScopes().get(0);

    private CallerContext callerContext;

    @BeforeEach
    void setUp() {
        callerContext = new CallerContext();
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    private JwtAuthenticationToken jwtToken(final Map<String, Object> claims, final String... authorities) {
        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(TOKEN_TTL_SECONDS),
                Map.of("alg", "none"), claims);
        return new JwtAuthenticationToken(jwt,
                java.util.Arrays.stream(authorities).map(SimpleGrantedAuthority::new).toList());
    }

    private void forwardIdentity(final CallerIdentity identity) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(Constants.CALLER_IDENTITY_ATTRIBUTE, identity);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void shouldReturnUserIdClaimWhenJwtHasIt() {
        SecurityContextHolder.getContext().setAuthentication(
                jwtToken(Map.of(Constants.USER_ID_CLAIM, "user-1", Constants.SUBJECT_CLAIM, "sub-1")));

        assertThat(callerContext.getUserId()).isEqualTo("user-1");
    }

    @Test
    void shouldFallBackToSubjectClaimWhenJwtHasNoUserId() {
        SecurityContextHolder.getContext().setAuthentication(
                jwtToken(Map.of(Constants.SUBJECT_CLAIM, "sub-1")));

        assertThat(callerContext.getUserId()).isEqualTo("sub-1");
    }

    @Test
    void shouldReturnForwardedUserIdWhenAuthenticationIsNotJwt() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("svc", "x"));
        forwardIdentity(new CallerIdentity("forwarded-1", Set.of(ELEVATED)));

        assertThat(callerContext.getUserId()).isEqualTo("forwarded-1");
    }

    @Test
    void shouldReturnAuthenticationNameWhenNoForwardedIdentity() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("svc", "x"));

        assertThat(callerContext.getUserId()).isEqualTo("svc");
    }

    @Test
    void shouldReturnNullUserIdWhenNoAuthentication() {
        assertThat(callerContext.getUserId()).isNull();
    }

    @Test
    void shouldReturnJwtAuthoritiesIgnoringForwardedIdentity() {
        SecurityContextHolder.getContext().setAuthentication(
                jwtToken(Map.of(Constants.SUBJECT_CLAIM, "sub-1"), "A", "B"));
        forwardIdentity(new CallerIdentity("forwarded-1", Set.of(ELEVATED)));

        assertThat(callerContext.getAuthorities()).containsExactlyInAnyOrder("A", "B");
    }

    @Test
    void shouldReturnForwardedAuthoritiesWhenAuthenticationIsNotJwt() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("svc", "x", "LOCAL"));
        forwardIdentity(new CallerIdentity("forwarded-1", Set.of("F")));

        assertThat(callerContext.getAuthorities()).containsExactly("F");
    }

    @Test
    void shouldReturnEmptyAuthoritiesWhenForwardedIdentityHasNone() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("svc", "x"));
        forwardIdentity(new CallerIdentity("forwarded-1", null));

        assertThat(callerContext.getAuthorities()).isEmpty();
    }

    @Test
    void shouldReturnAuthenticationAuthoritiesWhenNoForwardedIdentity() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("svc", "x", "LOCAL"));

        assertThat(callerContext.getAuthorities()).containsExactly("LOCAL");
    }

    @Test
    void shouldReturnEmptyAuthoritiesWhenNoAuthentication() {
        assertThat(callerContext.getAuthorities()).isEmpty();
    }

    @Test
    void shouldBeElevatedWhenAnyElevatedScopeIsPresent() {
        SecurityContextHolder.getContext().setAuthentication(
                jwtToken(Map.of(Constants.SUBJECT_CLAIM, "sub-1"), ELEVATED));

        assertThat(callerContext.isElevated()).isTrue();
    }

    @Test
    void shouldNotBeElevatedWhenNoElevatedScopeIsPresent() {
        SecurityContextHolder.getContext().setAuthentication(
                jwtToken(Map.of(Constants.SUBJECT_CLAIM, "sub-1"), "SOMETHING_ELSE"));

        assertThat(callerContext.isElevated()).isFalse();
    }
}

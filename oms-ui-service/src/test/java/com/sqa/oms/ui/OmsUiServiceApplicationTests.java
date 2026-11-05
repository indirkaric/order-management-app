package com.sqa.oms.ui;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;

/**
 * Smoke test proving every bean of the application context can be wired - this is exactly the
 * kind of check that would have caught a missing service implementation or a misspelled
 * configuration property before the application was ever deployed.
 */
@SpringBootTest
class OmsUiServiceApplicationTests {

    // The real JwtDecoder auto-configuration resolves the issuer-uri over the network at startup,
    // which this test environment cannot reach, so it is replaced with a mock just for context load.
    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void contextLoads() {
    }
}

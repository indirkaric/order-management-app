package com.sqa.oms.auditlog.constants;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;

/**
 * Class with all constants.
 */
public final class Constants {

    public static final String SQA_CLIENT_ID_HEADER = "Sqa-Client-Id";

    public static final String GATEWAY_AUDIT_LOG_CLIENT = "Gateway.AuditLog";

    public static final String BEARER_KEY_VALUE = "bearer-key";

    public static final String SECURITY_SCHEME = "bearer";

    public static final String SECURITY_FORMAT = "JWT";

    public static final String APP_NAME = "audit_log_app";

    public static final int DEFAULT_PAGE_SIZE = 50;

    public static final int LIMIT_PAGE_SIZE = 2000;

    private Constants() {
    }

    /**
     * Scopes exposed by Keycloak in the JWT scope claim.
     */
    public static final class ApiScopes {

        public static final String SCOPE = "SCOPE_";

        public static final String AUDIT_READ = "AUDIT_READ";

        public static final String AUDIT_READ_SCOPE = SCOPE + AUDIT_READ;

        private ApiScopes() {
        }
    }

    /**
     * Authority expressions used by method security and granted authority factories.
     */
    public static final class Authority {

        public static final String AUDIT_READ_AUTHORITIES = "hasAuthority("
                + "'" + ApiScopes.AUDIT_READ_SCOPE + "'"
                + ")";

        private Authority() {
        }

        /**
         * Returns the authorities granted to a trusted gateway client.
         *
         * @return list of granted authorities
         */
        public static List<SimpleGrantedAuthority> auditReadAuthorities() {
            return Collections.singletonList(new SimpleGrantedAuthority(ApiScopes.AUDIT_READ_SCOPE));
        }
    }
}

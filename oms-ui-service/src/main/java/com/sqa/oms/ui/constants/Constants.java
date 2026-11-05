package com.sqa.oms.ui.constants;

/**
 * Class with all constants.
 */
public final class Constants {

    public static final String BEARER_KEY_VALUE = "bearer-key";

    public static final String SECURITY_SCHEME = "bearer";

    public static final String SECURITY_FORMAT = "JWT";

    public static final String SQA_CLIENT_ID_HEADER = "Sqa-Client-Id";

    public static final String SQA_USER_ID_HEADER = "Sqa-User-Id";

    public static final String GATEWAY_ORDER_CLIENT = "Gateway.Order";

    public static final String GATEWAY_IDENTITY_CLIENT = "Gateway.Identity";

    public static final String GATEWAY_AUDIT_LOG_CLIENT = "Gateway.AuditLog";

    public static final String ORDER_SERVICE_NAME = "Order";

    public static final String IDENTITY_SERVICE_NAME = "Identity";

    public static final String AUDIT_LOG_SERVICE_NAME = "Audit-Log";

    public static final String USER_ID_CLAIM = "user_id";

    private Constants() {
    }

    /**
     * Scope names exactly as the backend services enforce them.
     */
    public static final class ApiScopes {

        public static final String SCOPE = "SCOPE_";

        public static final String SCOPE_ORDER_READ = SCOPE + "ORDER_READ";

        public static final String SCOPE_ORDER_WRITE = SCOPE + "ORDER_WRITE";

        public static final String SCOPE_ORDER_DELETE = SCOPE + "ORDER_DELETE";

        public static final String SCOPE_USER_READ = SCOPE + "USER_READ";

        public static final String SCOPE_USER_MANAGE = SCOPE + "USER_MANAGE";

        public static final String SCOPE_AUDIT_READ = SCOPE + "AUDIT_READ";

        public static final String SCOPE_PRODUCT_READ = SCOPE + "PRODUCT_READ";

        public static final String SCOPE_PRODUCT_WRITE = SCOPE + "PRODUCT_WRITE";

        private ApiScopes() {
        }
    }

    /**
     * Spring Security expressions used by the controllers.
     */
    public static final class Authority {

        public static final String ORDER_READ_AUTHORITY =
                "hasAuthority('" + ApiScopes.SCOPE_ORDER_READ + "')";

        public static final String ORDER_WRITE_AUTHORITY =
                "hasAuthority('" + ApiScopes.SCOPE_ORDER_WRITE + "')";

        public static final String ORDER_DELETE_AUTHORITY =
                "hasAuthority('" + ApiScopes.SCOPE_ORDER_DELETE + "')";

        public static final String USER_READ_AUTHORITY =
                "hasAuthority('" + ApiScopes.SCOPE_USER_READ + "')";

        public static final String USER_MANAGE_AUTHORITY =
                "hasAuthority('" + ApiScopes.SCOPE_USER_MANAGE + "')";

        public static final String AUDIT_READ_AUTHORITY =
                "hasAuthority('" + ApiScopes.SCOPE_AUDIT_READ + "')";

        public static final String USER_READ_OR_MANAGE_AUTHORITY = "hasAnyAuthority('"
                + ApiScopes.SCOPE_USER_READ + "','" + ApiScopes.SCOPE_USER_MANAGE + "')";

        public static final String PRODUCT_READ_AUTHORITY =
                "hasAuthority('" + ApiScopes.SCOPE_PRODUCT_READ + "')";

        public static final String PRODUCT_WRITE_AUTHORITY =
                "hasAuthority('" + ApiScopes.SCOPE_PRODUCT_WRITE + "')";

        private Authority() {
        }
    }
}

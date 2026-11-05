package com.sqa.oms.order.constants;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

/**
 * Class with all constants.
 */
public final class Constants {

    public static final String SQA_CLIENT_ID_HEADER = "Sqa-Client-Id";

    public static final String SQA_USER_ID_HEADER = "Sqa-User-Id";

    public static final String SOURCE_SERVICE = "order-service";

    public static final String SYSTEM_ACTOR = "system";

    public static final String USER_ID_CLAIM = "user_id";

    public static final String SUBJECT_CLAIM = "sub";

    public static final String GATEWAY_ORDER_CLIENT = "Gateway.Order";

    public static final String ORDER_STORAGE_CLIENT = "Order.Storage";

    public static final String CALLER_IDENTITY_ATTRIBUTE = "com.sqa.oms.order.callerIdentity";

    public static final String ORDER_NUMBER_PREFIX = "ORD-";

    public static final int ENTITY_ID_LENGTH = 8;

    public static final String ENTITY_ID_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public static final String BEARER_KEY_VALUE = "bearer-key";

    public static final String SECURITY_SCHEME = "bearer";

    public static final String SECURITY_FORMAT = "JWT";

    private Constants() {
    }

    /**
     * Permission names as they are published by Keycloak inside the JWT scope claim.
     */
    public static final class ApiScopes {

        public static final String SCOPE = "SCOPE_";

        public static final String ORDER_READ = SCOPE + "ORDER_READ";

        public static final String ORDER_WRITE = SCOPE + "ORDER_WRITE";

        public static final String ORDER_DELETE = SCOPE + "ORDER_DELETE";

        public static final String PRODUCT_READ = SCOPE + "PRODUCT_READ";

        public static final String PRODUCT_WRITE = SCOPE + "PRODUCT_WRITE";

        public static final String USER_READ = SCOPE + "USER_READ";

        public static final String AUDIT_READ = SCOPE + "AUDIT_READ";

        private ApiScopes() {
        }
    }

    /**
     * Ready made expressions for method level security.
     */
    public static final class Authority {

        public static final String ORDER_READ_AUTHORITY = "hasAuthority('" + ApiScopes.ORDER_READ + "')";

        public static final String ORDER_WRITE_AUTHORITY = "hasAuthority('" + ApiScopes.ORDER_WRITE + "')";

        public static final String ORDER_DELETE_AUTHORITY = "hasAuthority('" + ApiScopes.ORDER_DELETE + "')";

        public static final String PRODUCT_READ_AUTHORITY = "hasAuthority('" + ApiScopes.PRODUCT_READ + "')";

        public static final String PRODUCT_WRITE_AUTHORITY = "hasAuthority('" + ApiScopes.PRODUCT_WRITE + "')";

        private Authority() {
        }

        /**
         * Authorities granted to a request arriving through the trusted API gateway.
         *
         * @return list of granted authorities
         */
        public static List<SimpleGrantedAuthority> gatewayAuthorities() {
            return List.of(
                    new SimpleGrantedAuthority(ApiScopes.ORDER_READ),
                    new SimpleGrantedAuthority(ApiScopes.ORDER_WRITE),
                    new SimpleGrantedAuthority(ApiScopes.ORDER_DELETE),
                    new SimpleGrantedAuthority(ApiScopes.PRODUCT_READ),
                    new SimpleGrantedAuthority(ApiScopes.PRODUCT_WRITE)
            );
        }

        /**
         * Authorities that only administrative roles ever hold. Used to decide whether a caller
         * may look beyond its own orders.
         *
         * @return list of elevated scope names
         */
        public static List<String> elevatedScopes() {
            return List.of(ApiScopes.ORDER_DELETE, ApiScopes.USER_READ, ApiScopes.AUDIT_READ);
        }
    }
}

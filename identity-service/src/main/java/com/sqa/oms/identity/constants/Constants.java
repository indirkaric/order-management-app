package com.sqa.oms.identity.constants;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

/**
 * Class with all constants.
 */
public final class Constants {

    public static final String SQA_CLIENT_ID_HEADER = "Sqa-Client-Id";

    public static final String SQA_USER_ID_HEADER = "Sqa-User-Id";

    public static final String CALLER_IDENTITY_ATTRIBUTE = "com.sqa.oms.identity.callerIdentity";

    public static final String ENTITY_ID = "entityId";

    public static final String GATEWAY_IDENTITY_CLIENT = "Gateway.Identity";

    public static final String SOURCE_SERVICE = "identity-service";

    public static final String SYSTEM_ACTOR = "system";

    public static final String APP_NAME = "identity_app";

    public static final String BEARER_KEY_VALUE = "bearer-key";

    public static final String SECURITY_SCHEME = "bearer";

    public static final String SECURITY_FORMAT = "JWT";

    public static final String SUPER_ADMIN_GROUP_ENTITY_ID = "SUPER-ADMIN";

    public static final int ENTITY_ID_LENGTH = 8;

    public static final int PAGE_SIZE_DEFAULT_VALUE = 50;

    public static final String ENTITY_ID_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private Constants() {
    }

    /**
     * Status values shared by every entity of this service.
     */
    public static final class Status {

        public static final short INACTIVE_STATUS = 0;

        public static final short ACTIVE_STATUS = 1;

        private Status() {
        }
    }

    /**
     * Permission names, which Keycloak also exposes as client scopes.
     */
    public static final class ApiScopes {

        public static final String SCOPE_PREFIX = "SCOPE_";

        public static final String USER_READ = "USER_READ";

        public static final String USER_MANAGE = "USER_MANAGE";

        public static final String SCOPE_USER_READ = SCOPE_PREFIX + USER_READ;

        public static final String SCOPE_USER_MANAGE = SCOPE_PREFIX + USER_MANAGE;

        private ApiScopes() {
        }
    }

    /**
     * Authority expressions used by the {@code @PreAuthorize} annotations.
     */
    public static final class Authority {

        public static final String USER_READ_OR_MANAGE = "hasAnyAuthority("
                + "'" + ApiScopes.SCOPE_USER_READ + "',"
                + "'" + ApiScopes.SCOPE_USER_MANAGE + "'"
                + ")";

        public static final String USER_MANAGE = "hasAuthority("
                + "'" + ApiScopes.SCOPE_USER_MANAGE + "'"
                + ")";

        private Authority() {
        }

        /**
         * Returns the authorities granted to a trusted internal caller.
         *
         * @return the full list of authorities of this service
         */
        public static List<SimpleGrantedAuthority> fullAuthorities() {
            return List.of(
                    new SimpleGrantedAuthority(ApiScopes.SCOPE_USER_READ),
                    new SimpleGrantedAuthority(ApiScopes.SCOPE_USER_MANAGE)
            );
        }
    }
}

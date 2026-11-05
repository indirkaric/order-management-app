package com.sqa.oms.storage.constants;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;

/**
 * Class with all constants.
 */
public final class Constants {

    public static final String BEARER_KEY_VALUE = "bearer-key";

    public static final String SECURITY_SCHEME = "bearer";

    public static final String SECURITY_FORMAT = "JWT";

    public static final String SQA_CLIENT_ID_HEADER = "Sqa-Client-Id";

    public static final String APP_NAME = "oms_storage_app";

    public static final String ORDER_STORAGE_CLIENT = "Order.Storage";

    private Constants() {
    }

    public static final class ApiScopes {

        public static final String SCOPE = "SCOPE_";

        public static final String PRODUCT_READ = "PRODUCT_READ";

        public static final String PRODUCT_WRITE = "PRODUCT_WRITE";

        public static final String PRODUCT_READ_SCOPE = SCOPE + PRODUCT_READ;

        public static final String PRODUCT_WRITE_SCOPE = SCOPE + PRODUCT_WRITE;

        private ApiScopes() {
        }
    }

    public static final class Authority {

        public static final String PRODUCT_WRITE_AUTHORITY = "hasAuthority("
                + "'" + ApiScopes.PRODUCT_WRITE_SCOPE + "'"
                + ")";

        public static final String PRODUCT_READ_AUTHORITY = "hasAuthority("
                + "'" + ApiScopes.PRODUCT_READ_SCOPE + "'"
                + ")";

        public static final String PRODUCT_RESTRICTED_AUTHORITIES = "hasAnyAuthority("
                + "'" + ApiScopes.PRODUCT_READ_SCOPE + "',"
                + "'" + ApiScopes.PRODUCT_WRITE_SCOPE + "'"
                + ")";

        private Authority() {
        }

        /**
         * Return the authorities granted to the order service when it calls this service.
         *
         * @return list of granted authorities
         */
        public static List<SimpleGrantedAuthority> orderServiceAuthorities() {

            return new ArrayList<>() {
                {
                    add(new SimpleGrantedAuthority(ApiScopes.PRODUCT_READ_SCOPE));
                    add(new SimpleGrantedAuthority(ApiScopes.PRODUCT_WRITE_SCOPE));
                }
            };
        }
    }
}

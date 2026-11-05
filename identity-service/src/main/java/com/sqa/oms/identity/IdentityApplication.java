package com.sqa.oms.identity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the identity service.
 */
@SpringBootApplication
public class IdentityApplication {

    protected IdentityApplication() {
    }

    /**
     * Boots the Spring application context.
     *
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        SpringApplication.run(IdentityApplication.class, args);
    }
}

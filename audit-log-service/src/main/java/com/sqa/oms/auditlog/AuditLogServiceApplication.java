package com.sqa.oms.auditlog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the audit log service.
 */
@SpringBootApplication
public class AuditLogServiceApplication {

    /**
     * Starts the Spring Boot application.
     *
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        SpringApplication.run(AuditLogServiceApplication.class, args);
    }
}

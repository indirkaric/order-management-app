package com.sqa.oms.ui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the OMS UI (gateway) service.
 */
@SpringBootApplication
public class OmsUiServiceApplication {

    protected OmsUiServiceApplication() {
    }

    /**
     * Starts the application.
     *
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        SpringApplication.run(OmsUiServiceApplication.class, args);
    }
}

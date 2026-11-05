package com.sqa.oms.storage;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@OpenAPIDefinition(servers = {@Server(url = "/", description = "Default Server URL")})
@SpringBootApplication
public class OmsStorageServiceApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(OmsStorageServiceApplication.class);

    /**
     * Application entry point.
     *
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        SpringApplication app = new SpringApplication(OmsStorageServiceApplication.class);
        Environment env = app.run(args).getEnvironment();
        LOGGER.info("Application started successfully. Access URLs:"
                        + "Local: http://localhost:{}",
                env.getProperty("server.port"));
    }

}

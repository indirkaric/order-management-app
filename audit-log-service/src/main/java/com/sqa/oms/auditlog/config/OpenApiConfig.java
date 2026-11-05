package com.sqa.oms.auditlog.config;

import com.sqa.oms.auditlog.constants.Constants;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI configuration.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Creates the Spring bean of OpenApi used by the application.
     *
     * @return customOpenApi
     */
    @Bean
    public OpenAPI customOpenAPI() {
        Info info = new Info()
                .title("oms-audit-log-API definition")
                .version("1.0")
                .description("This API exposes endpoints for querying the order management audit log.");

        return new OpenAPI()
                .info(info)
                .components(new Components()
                        .addSecuritySchemes(Constants.BEARER_KEY_VALUE,
                                new SecurityScheme().type(SecurityScheme.Type.HTTP)
                                        .scheme(Constants.SECURITY_SCHEME)
                                        .bearerFormat(Constants.SECURITY_FORMAT)));
    }
}

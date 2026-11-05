package com.sqa.oms.storage.config;

import com.sqa.oms.storage.constants.Constants;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
                .title("oms-storage-service-API definition")
                .version("1.0")
                .description("This API exposes endpoints for storing and retrieving product documentation files.");
        return new OpenAPI()
                .info(info)
                .components(new Components()
                        .addSecuritySchemes(Constants.BEARER_KEY_VALUE,
                                new SecurityScheme().type(SecurityScheme.Type.HTTP)
                                        .scheme(Constants.SECURITY_SCHEME)
                                        .bearerFormat(Constants.SECURITY_FORMAT)));
    }

}

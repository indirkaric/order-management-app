package com.sqa.oms.ui.config;

import com.sqa.oms.ui.constants.Constants;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI definition of the gateway API.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Builds the OpenAPI definition.
     *
     * @return OpenAPI
     */
    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("OMS UI Service")
                        .description("API gateway for the Order Management app")
                        .version("v1"))
                .components(new Components()
                        .addSecuritySchemes(Constants.BEARER_KEY_VALUE, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme(Constants.SECURITY_SCHEME)
                                .bearerFormat(Constants.SECURITY_FORMAT)));
    }
}

package com.sqa.oms.order.config;

import com.sqa.oms.order.constants.Constants;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    /**
     * Declares the bearer token security scheme used by every endpoint.
     *
     * @return OpenAPI
     */
    @Bean
    public OpenAPI customOpenApi() {
        return new OpenAPI()
                .info(new Info().title("Order Service API").version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(Constants.BEARER_KEY_VALUE))
                .components(new Components().addSecuritySchemes(Constants.BEARER_KEY_VALUE,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme(Constants.SECURITY_SCHEME)
                                .bearerFormat(Constants.SECURITY_FORMAT)));
    }
}

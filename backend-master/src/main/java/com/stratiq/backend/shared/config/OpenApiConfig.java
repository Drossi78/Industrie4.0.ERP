package com.stratiq.backend.shared.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    // SpringDoc warnings about /v3/api-docs and /swagger-ui.html endpoints are expected in development.
    info = @Info(
        title = "Stratiq Backend API",
        version = "0.0.1",
        description = "REST API for Stratiq inventory & order management.",
        contact = @Contact(name = "Stratiq Team")
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Local dev")
    }
)
public class OpenApiConfig {
}

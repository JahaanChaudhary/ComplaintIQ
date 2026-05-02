package com.complaintiq.config;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.security.*;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.*;
import java.util.List;
@Configuration
public class SwaggerConfig {
    private static final String SECURITY_SCHEME_NAME = "bearerAuth";
    @Bean
    public OpenAPI complaintIQOpenAPI() {
        return new OpenAPI()
            .info(new Info().title("ComplaintIQ API").description("AI-Powered Customer Complaint Management System").version("1.0.0")
                .contact(new Contact().name("ComplaintIQ Team").email("support@complaintiq.com"))
                .license(new License().name("MIT License")))
            .servers(List.of(new Server().url("http://localhost:8080").description("Development Server")))
            .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
            .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME,
                new SecurityScheme().name(SECURITY_SCHEME_NAME).type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
    }
}

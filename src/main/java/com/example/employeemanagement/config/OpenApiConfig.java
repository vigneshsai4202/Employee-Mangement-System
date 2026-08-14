package com.example.employeemanagement.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * @Configuration:
 * What    → Marks this class as a source of @Bean definitions.
 * Why     → Spring reads this class at startup and registers the beans
 *            returned by @Bean methods into the ApplicationContext.
 * Internal → @Configuration classes are themselves Spring-managed beans,
 *             and their @Bean methods are proxied to ensure singleton behavior.
 */
@Configuration
public class OpenApiConfig {

    /*
     * @Bean:
     * What    → Tells Spring that this method produces a bean to be managed
     *            by the ApplicationContext.
     * Why     → springdoc-openapi looks for an OpenAPI bean in the context.
     *            If found, it uses it to populate the API info section in Swagger UI.
     * Internal → Spring calls this method once at startup and caches the result.
     *             Every subsequent request for an OpenAPI bean gets the same instance.
     */
    @Bean
    public OpenAPI employeeManagementOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Employee Management System API")
                        .description("""
                                A production-style REST API for managing employees.
                                
                                Features:
                                - Create, read, update, and delete employees
                                - Search employees by name or email
                                - Filter employees by department
                                - Input validation with detailed error responses
                                - Global exception handling
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Employee Management Team")
                                .email("support@example.com"))
                        .license(new License()
                                .name("MIT License")));
    }
}

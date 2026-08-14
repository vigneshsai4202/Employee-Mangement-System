package com.example.employeemanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
 * @SpringBootApplication is a convenience annotation that combines three annotations:
 *
 * 1. @Configuration
 *    Marks this class as a source of Spring bean definitions.
 *    Spring will look here for @Bean methods.
 *
 * 2. @EnableAutoConfiguration
 *    Tells Spring Boot to automatically configure beans based on what's on the classpath.
 *    Example: sees mysql-connector-j + spring-data-jpa → auto-configures DataSource + EntityManagerFactory.
 *    Example: sees spring-boot-starter-web → auto-configures DispatcherServlet + embedded Tomcat.
 *
 * 3. @ComponentScan
 *    Scans this package and all sub-packages for Spring-managed components:
 *    @Component, @Service, @Repository, @Controller, @RestController, @Configuration
 *    This is why all our classes must be inside com.example.employeemanagement or its sub-packages.
 */
@SpringBootApplication
public class EmployeeManagementApplication {

    /*
     * SpringApplication.run() does the following:
     * 1. Creates an ApplicationContext (the Spring IoC container)
     * 2. Registers all beans discovered by @ComponentScan
     * 3. Triggers auto-configuration
     * 4. Starts the embedded Tomcat server on port 8080
     * 5. Publishes ApplicationReadyEvent when startup is complete
     */
    public static void main(String[] args) {
        SpringApplication.run(EmployeeManagementApplication.class, args);
    }
}

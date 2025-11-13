package com.usta.serviexpress;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ServiExpressApplication
 *
 * Purpose:
 * - Entry point for the ServiExpress Spring Boot application.
 * - Bootstraps the Spring context and starts the embedded server.
 *
 * Annotations:
 * - @SpringBootApplication:
 *   - Combines @Configuration, @EnableAutoConfiguration, and @ComponentScan.
 *   - Enables Spring Boot auto-configuration and component scanning in the current package.
 *
 * Important Notes:
 * - The main() method delegates to SpringApplication.run(), which initializes the
 *   application context, starts the embedded Tomcat server, and performs classpath scanning.
 * - Additional beans, configurations, or command-line runners can be defined in this class
 *   if needed for application startup logic.
 */
@SpringBootApplication
public class ServiExpressApplication {

    /**
     * Application entry point.
     *
     * @param args Command-line arguments passed to the application.
     *             Can be used to customize Spring Boot behavior or environment properties.
     */
    public static void main(String[] args) {
        SpringApplication.run(ServiExpressApplication.class, args);
    }

}

/*
Summary (Technical Note):
This is the main class that bootstraps the ServiExpress Spring Boot application. It
initializes the Spring context, triggers auto-configuration, and starts the embedded
server, making the application ready to accept HTTP requests.
*/

package com.usta.serviexpress.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * AppConfig
 *
 * Purpose:
 * - Provides application-wide Spring configuration beans.
 * - Centralizes commonly used components that can be injected across the application.
 *
 * Current Configuration:
 * - RestTemplate bean for making HTTP requests to external services.
 *
 * Notes:
 * - The RestTemplate bean can be autowired anywhere in the application to perform synchronous REST calls.
 * - Consider using WebClient for reactive/non-blocking HTTP calls in future versions.
 */
@Configuration
public class AppConfig {

    /**
     * Creates a RestTemplate bean.
     *
     * Purpose:
     * - Allows dependency injection of RestTemplate throughout the application.
     *
     * @return a new instance of RestTemplate.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

/*
Summary (Technical Note):
AppConfig defines application-level Spring beans. 
Currently, it provides a RestTemplate for synchronous HTTP communication, 
enabling other services or components to perform REST calls via dependency injection.
*/

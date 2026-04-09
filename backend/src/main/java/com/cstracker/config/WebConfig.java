/**
 * WebConfig.java
 *
 * Spring MVC configuration for the CS2 Investment Tracker backend.
 * Registers CORS rules for the React frontend and provides a shared RestTemplate bean.
 *
 * @author Ville Laaksoaho
 */
package com.cstracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configures CORS to allow the React dev server to call the backend API.
     * Permits all standard HTTP methods and headers on all /api/** routes.
     *
     * @param registry the Spring CORS registry to add mappings to
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000", "http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*");
    }

    /**
     * Provides a shared RestTemplate bean used for outbound HTTP calls to the Steam API.
     *
     * @return a default RestTemplate instance
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

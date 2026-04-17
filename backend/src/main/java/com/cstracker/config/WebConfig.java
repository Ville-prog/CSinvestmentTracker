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
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;

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
                .allowedOrigins("https://csinvestmenttracker.vercel.app")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*");
    }

    /**
     * Provides a shared RestTemplate bean used for outbound HTTP calls to the Steam API.
     * Connect and read timeouts prevent the nightly job from hanging indefinitely on a stalled Steam connection.
     *
     * @return a RestTemplate with bounded connect and read timeouts
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(10).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(30).toMillis());
        return new RestTemplate(factory);
    }
}

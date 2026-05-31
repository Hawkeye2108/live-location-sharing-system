package com.org.livelocationsharing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Which frontend URLs are allowed to call this backend
        config.setAllowedOrigins(allowedOrigins);

        // Allow all standard HTTP methods
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Allow all headers (Content-Type, Authorization, etc.)
        config.setAllowedHeaders(List.of("*"));

        // Allow cookies / auth headers if needed later
        config.setAllowCredentials(true);

        // Cache preflight response for 1 hour (reduces OPTIONS requests)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);  // apply to all endpoints

        return new CorsFilter(source);
    }
}
package com.tracksuit.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("=== Configuring SecurityFilterChain (Manual OAuth - no oauth2Login) ===");

        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/v1/gmail/**",
                    "/api/v1/orders/**",
                    "/oauth2/**",
                    "/login/**",
                    "/error"
                ).permitAll()
                .anyRequest().permitAll()  // Allow all for development
            );
        // NOTE: No .oauth2Login() — we handle OAuth manually in GmailController

        log.info("=== SecurityFilterChain configured successfully ===");
        return http.build();
    }
}

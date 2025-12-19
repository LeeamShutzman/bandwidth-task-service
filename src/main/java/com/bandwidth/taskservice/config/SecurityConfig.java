package com.bandwidth.taskservice.config;

import com.bandwidth.taskservice.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    // IMPORTANT: Copy your ALLOWED_ORIGINS from the Auth Service here
    private static final List<String> ALLOWED_ORIGINS = Arrays.asList(
            "http://localhost:8081",
            "http://localhost:19006",
            "http://10.0.0.5:8081"
            // Add any other origins your frontend uses
    );

    @Bean
    public PasswordEncoder passwordEncoder() {
        // We use BCrypt as it includes a work factor/salt and is designed specifically for password hashing.
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable()) // Stateless APIs typically disable CSRF
                .authorizeHttpRequests(auth -> auth
                        // All requests must be authenticated
                        .requestMatchers("/api/v1/tasks/**").authenticated()
                        .anyRequest().permitAll() // Fallback to permit all if path doesn't match
                )
                .sessionManagement(session -> session
                        // Stateless session management (no server-side sessions)
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // Add the JWT filter BEFORE the standard Spring filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(ALLOWED_ORIGINS);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}




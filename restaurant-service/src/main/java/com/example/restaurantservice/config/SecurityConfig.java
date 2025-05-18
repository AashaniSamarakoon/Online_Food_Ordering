package com.example.restaurantservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;
import java.util.Base64;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Custom JWT converter
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();

        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthorityPrefix(""); // Remove all prefixes
        authoritiesConverter.setAuthoritiesClaimName("roles"); // Look for "roles" claim

        jwtConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(SecurityConstants.PUBLIC_URLS).permitAll()
                        .requestMatchers("/api/health").permitAll()
                        // Menu Restaurant public endpoints configuration
                        .requestMatchers(HttpMethod.GET, "/api/menu-restaurant/restaurants/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/menu-restaurant/all").permitAll()
                        // Important: Allow /api/neworder to be accessible without authentication
                        .requestMatchers("/api/neworder").permitAll()
                        // Protected endpoints
                        .requestMatchers(HttpMethod.GET, "/api/menu-restaurant/my-restaurant").hasAuthority("RESTAURANT_ADMIN")
                        .requestMatchers("/api/menu-items/**").hasAuthority("RESTAURANT_ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder())
                                .jwtAuthenticationConverter(jwtConverter)
                        )
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        String secretKey = "Kc7reQ2mi33d++2B/paehH1EJCA456cvhbSDFGdfghjk86fghjhjxzcfvFGH5KM7gLRx0Ts=";
        byte[] decodedKey = Base64.getDecoder().decode(secretKey);
        SecretKey key = new SecretKeySpec(decodedKey, 0, decodedKey.length, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).build();
    }
}
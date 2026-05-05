package com.complaintiq.config;

import com.complaintiq.auth.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:8080,http://localhost:5173}")
    private String allowedOrigins;

    private static final String[] PUBLIC_URLS = {
            "/api/auth/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api-docs/**",
            "/actuator/health",
            "/ws/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_URLS).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/complaints/track/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/complaints").hasAnyRole("CUSTOMER", "AGENT", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/complaints/my").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.POST, "/api/complaints/*/feedback").hasAnyRole("CUSTOMER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/complaints").hasAnyRole("AGENT", "TEAM_LEAD", "MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/complaints/*").hasAnyRole("CUSTOMER", "AGENT", "TEAM_LEAD", "MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/complaints/*/status").hasAnyRole("AGENT", "TEAM_LEAD", "MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/complaints/*/resolve").hasAnyRole("AGENT", "TEAM_LEAD", "MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/complaints/*/reassign").hasAnyRole("TEAM_LEAD", "MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/complaints/*/escalate").hasAnyRole("TEAM_LEAD", "MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/agents", "/api/agents/**").hasAnyRole("AGENT", "TEAM_LEAD", "MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/agents").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/agents/*/availability").hasAnyRole("AGENT", "TEAM_LEAD", "MANAGER", "ADMIN")
                        .requestMatchers("/api/analytics/**").hasAnyRole("MANAGER", "ADMIN")
                        .requestMatchers("/api/departments/**").hasAnyRole("MANAGER", "ADMIN")
                        .anyRequest().authenticated())
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
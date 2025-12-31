package com.grievance.auth_service.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.grievance.auth_service.security.JwtAuthEntryPoint;
import com.grievance.auth_service.security.JwtAuthFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Public API
                        .requestMatchers(
                                "/api/v1/auth/register",
                                "/api/v1/auth/login"
                        ).permitAll()

                        // Authenticated but role-agnostic endpoints
                        .requestMatchers("/api/v1/auth/me")
                                .hasAnyAuthority(
                                        "CITIZEN",
                                        "DEPARTMENT_OFFICER",
                                        "SUPERVISORY_OFFICER",
                                        "SYSTEM_ADMIN"
                                )

                        // Add more role-protected endpoints later
                        //.requestMatchers("/api/v1/admin/**").hasAuthority("SYSTEM_ADMIN")
                        //.requestMatchers("/api/v1/grievance/assign/**").hasAnyAuthority("DEPARTMENT_OFFICER","SUPERVISORY_OFFICER")

                        // Everything else must be authenticated
                        .anyRequest().authenticated()
                )

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthEntryPoint)   // <-- add this
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}




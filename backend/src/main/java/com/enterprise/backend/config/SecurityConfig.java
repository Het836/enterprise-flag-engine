package com.enterprise.backend.config;

import com.enterprise.backend.security.ApiKeyAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ApiKeyAuthFilter apiKeyAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception{
        httpSecurity
                .csrf(csrf -> csrf.disable()) // Disabled for stateless API architectures
                .authorizeHttpRequests(auth -> auth
                        // 1. Machine Layer (Bypasses human login, relies on API Key Filter)
                        .requestMatchers("/api/v1/sdk/**").permitAll()

                        // 2. Human Read Layer (Explicitly check for GET)
                        .requestMatchers(HttpMethod.GET, "/api/v1/flags/**").hasAnyRole("VIEWER", "ADMIN")

                        // 3. Human Write Layer (Explicitly check for POST, PUT, PATCH, DELETE)
                        .requestMatchers(HttpMethod.POST, "/api/v1/flags/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/flags/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/flags/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/flags/**").hasRole("ADMIN")

                        // 4. Global Catch-all Lockdown
                        .anyRequest().authenticated()
                )
                // 3.Enable standard Basic Authentication protocol headers
                .httpBasic(Customizer.withDefaults())
                // Inject our API Key validator right before standard username/password processing
                .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    @Bean
    public UserDetailsService userDetails(){
        // Create a read-only viewer profile
        UserDetails viewer = User.withDefaultPasswordEncoder()
                .username("viewer")
                .password("password")
                .roles("VIEWER")
                .build();

        // Create a privileged admin profile
        UserDetails admin = User.withDefaultPasswordEncoder()
                .username("admin")
                .password("password")
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(viewer, admin);
    }
}

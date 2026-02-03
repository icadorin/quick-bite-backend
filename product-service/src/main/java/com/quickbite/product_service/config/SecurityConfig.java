package com.quickbite.product_service.config;

import com.quickbite.core.security.UserRole;
import com.quickbite.product_service.constants.PublicEndPoints;
import com.quickbite.product_service.constants.SecuredEndPoints;
import com.quickbite.product_service.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    PublicEndPoints.PRODUCTS,
                    PublicEndPoints.RESTAURANTS,
                    PublicEndPoints.API_PRODUCTS,
                    PublicEndPoints.API_RESTAURANTS,
                    PublicEndPoints.ACTUATOR_HEALTH,
                    PublicEndPoints.ERROR
                ).permitAll()
                .requestMatchers(SecuredEndPoints.ADMIN)
                    .hasRole(UserRole.ADMIN.name())
                .requestMatchers(SecuredEndPoints.RESTAURANT_MANAGEMENT)
                    .hasAnyRole(
                        UserRole.ADMIN.name(),
                        UserRole.RESTAURANT_OWNER.name()
                    )
                .anyRequest().authenticated()
            )
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            )
            .build();

        return http.build();
    }
}

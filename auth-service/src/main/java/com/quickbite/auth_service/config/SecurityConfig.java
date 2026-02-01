package com.quickbite.auth_service.config;

import com.quickbite.auth_service.constants.PublicEndPoints;
import com.quickbite.auth_service.constants.SecuredEndPoints;
import com.quickbite.auth_service.security.JwtAuthenticationFilter;
import com.quickbite.core.security.UserRole;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
        AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((
                    request,
                    response,
                    authException
                    ) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"))
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    PublicEndPoints.AUTH,
                    PublicEndPoints.API_AUTH,
                    PublicEndPoints.ACTUATOR_HEALTH,
                    PublicEndPoints.ERROR
                ).permitAll()
                .requestMatchers(SecuredEndPoints.ADMIN)
                    .hasRole(UserRole.ADMIN.name())
                .requestMatchers(SecuredEndPoints.RESTAURANT)
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
    }
}

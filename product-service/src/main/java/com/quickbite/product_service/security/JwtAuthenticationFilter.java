package com.quickbite.product_service.security;

import com.quickbite.core.exception.BaseBusinessException;
import com.quickbite.core.security.UserRole;
import com.quickbite.product_service.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final HandlerExceptionResolver resolver;

    public JwtAuthenticationFilter(
        JwtService jwtService,
        @Qualifier("handlerExceptionResolver")
        HandlerExceptionResolver resolver
    ) {
        this.jwtService = jwtService;
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7);

            jwtService.validateToken(token);

            String email = jwtService.getEmailFromToken(token);
            UserRole userRole = jwtService.getUserRoleFromToken(token);
            Long userId = jwtService.getUserIdFromToken(token);

            AuthenticatedUser user =
                new AuthenticatedUser(userId, email);

            var authorities = List.of(
                new SimpleGrantedAuthority(userRole.getAuthority())
            );

            var auth = new UsernamePasswordAuthenticationToken(
                user,
                null,
                authorities
            );

            SecurityContextHolder.getContext().setAuthentication(auth);

            filterChain.doFilter(request, response);

        } catch (AuthenticationException | BaseBusinessException ex) {
            SecurityContextHolder.clearContext();
            resolver.resolveException(request, response, null, ex);
        }
    }
}

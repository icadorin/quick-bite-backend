package com.quickbite.auth_service.security;

import com.quickbite.auth_service.entity.User;
import com.quickbite.auth_service.repository.UserRepository;
import com.quickbite.auth_service.service.JwtService;
import com.quickbite.core.exception.BaseBusinessException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Qualifier("handlerExceptionResolver")
    private final HandlerExceptionResolver resolver;

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

        String token = authHeader.substring(7);

        try {
            Authentication auth = buildAuthentication(token);

            if (auth != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {
                SecurityContextHolder.getContext()
                    .setAuthentication(auth);
            }

            filterChain.doFilter(request, response);
        } catch (AuthenticationException | BaseBusinessException ex) {
            resolver.resolveException(request, response, null, ex);
        }
    }

    private UsernamePasswordAuthenticationToken buildAuthentication(String token) {

        jwtService.validateToken(token);

        String email = jwtService.getEmailFromToken(token);

        if (email == null) {
            return null;
        }

        User user = userRepository.findByEmail(email)
            .orElse(null);

        if (user == null) {
            return null;
        }

        return new UsernamePasswordAuthenticationToken(
            user,
            null,
            buildAuthorities(user)
        );
    }

    private List<SimpleGrantedAuthority> buildAuthorities(User user) {
        return List.of(
            new SimpleGrantedAuthority(user.getRole().getAuthority())
        );
    }
}

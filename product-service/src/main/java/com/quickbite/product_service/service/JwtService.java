package com.quickbite.product_service.service;

import com.quickbite.core.exception.InvalidTokenException;
import com.quickbite.core.security.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;


@Service
public class JwtService {

    private final SecretKey secretKey;

    public JwtService(
        @Value("${security.jwt.secret}") String secret
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public void validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);
        } catch (ExpiredJwtException ex) {
            throw new InvalidTokenException("Token expired");
        } catch (JwtException ex) {
            throw new InvalidTokenException("Token invalid");
        }
    }

    public String getRoleFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("role", String.class);
    }

    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public Long getUserIdFromToken(String token) {
        Object value = getClaims(token).get("userId");

        if (value == null) {
            throw new InvalidTokenException("Token missing userId");
        }

        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException ex) {
            throw new InvalidTokenException("Token has invalid userId");
        }
    }

    public UserRole getUserRoleFromToken(String token) {
        String role = getRoleFromToken(token);

        if (role == null || role.isBlank()) {
            throw new InvalidTokenException("Role not found in token");
        }

        try {
            return UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidTokenException("Invalid role in token");
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
}

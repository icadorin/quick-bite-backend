package com.quickbite.auth_service.service;

import com.quickbite.auth_service.entity.User;
import com.quickbite.core.exception.InvalidTokenException;
import com.quickbite.core.exception.JwtValidationException;
import com.quickbite.core.security.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.JwtException;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Slf4j
@Service
public class JwtService {

    private static final int MILLISECONDS_TO_SECONDS = 1000;

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:3600000}")
    private Long expiration;

    @Value("${jwt.issuer:auth-service}")
    private String issuer;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
            .setSubject(user.getEmail())
            .claim("userId", user.getId())
            .claim("role", user.getRole().name())
            .claim("fullName", user.getFullName())
            .setIssuer(issuer)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(getSigningKey())
            .compact();
    }

    /**
     * @throws JwtException when token is invalid
     * @throws JwtValidationException when token is expired or issuer is invalid
     */
    public Claims validateAndExtractClaims(String token) {
        try {
            Claims claims = extractAllClaims(token);

            if (isTokenExpired(claims)) {
                throw new JwtValidationException("Token has expired");
            }

            if (!issuer.equals(claims.getIssuer())) {
                throw new JwtValidationException("Invalid token issuer");
            }

            return claims;
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtValidationException("Invalid token");
        }
    }

    public String getEmailFromToken(String token) {
        Claims claims = validateAndExtractClaims(token);
        return extractClaim(claims, Claims::getSubject);
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = validateAndExtractClaims(token);
        return extractClaim(claims,
            c -> c.get("userId", Long.class)
        );
    }

    public Long getTokenExpirationInSeconds() {
        return expiration / MILLISECONDS_TO_SECONDS;
    }

    public UserRole getUserRoleFromToken(String token) {
        Claims claims = validateAndExtractClaims(token);

        String role = extractClaim(claims,
            c -> c.get("role", String.class)
        );

        if (role == null || role.isBlank()) {
            throw new InvalidTokenException("Role not found in token");
        }

        try {
            return UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidTokenException("Invalid role in token");
        }
    }

    private <T> T extractClaim(
        Claims claims,
        Function<Claims, T> resolver
    ) {
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }
}

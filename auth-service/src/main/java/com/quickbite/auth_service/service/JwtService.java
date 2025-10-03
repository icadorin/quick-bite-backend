package com.quickbite.auth_service.service;

import com.quickbite.auth_service.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Slf4j
@Service
public class JwtService {

    private static final int MILLISECONDS_TO_SECONDS = 1000;

    @Value("${jwt.secret:fallbackSecretKeyForDevelopment}")
    private String secret;

    @Value("${jwt.expiration:3600000}")
    private Long expiration;

    @Value("${jwt.issuer:auth-service}")
    private String issuer;

    private SecretKey getSigningKey() {
        byte[] KeyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(KeyBytes);
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

    public boolean validateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);

            if (isTokenExpired(claims)) {
                log.warn("Token expirado para usuário: {}", claims.getSubject());
                return false;
            }

            if (!issuer.equals(claims.getIssuer())) {
                log.warn("Token com issuer inválido: {}", claims.getIssuer());
                return false;
            }

            return true;
        } catch (Exception e) {
            log.debug("Falha na validação do token: {}", e.getMessage());
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long getUserIdFromToken(String token) {
        return extractClaim(token, claim -> claim.get("userId", Long.class));
    }

    public Long getTokenExpirationInSeconds() {
        return expiration / MILLISECONDS_TO_SECONDS;
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
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

    private Date getExpirationDateFromToken(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

}

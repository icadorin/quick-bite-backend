package com.quickbite.auth_service.service;

import com.quickbite.auth_service.constants.TestConstants;
import com.quickbite.auth_service.entity.User;
import com.quickbite.core.exception.InvalidTokenException;
import com.quickbite.core.exception.JwtValidationException;
import com.quickbite.core.security.UserRole;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {

    private JwtService jwtService;
    private User user;

    @BeforeEach
    public void setUp() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(
            jwtService,
            "secret",
            "testSecretKeytestSecretKeytestSecretKey"
        );

        ReflectionTestUtils.setField(
            jwtService,
            "expiration",
            3600000L
        );

        ReflectionTestUtils.setField(
            jwtService,
            "issuer",
            "auth-service"
        );

        user = User.builder()
            .id(TestConstants.VALID_USER_ID)
            .email(TestConstants.VALID_EMAIL)
            .role(UserRole.CUSTOMER)
            .build();
    }

    @Test
    void generateToken_shouldGenerateValidToken() {
        String token = jwtService.generateToken(user);

        assertNotNull(token);

        assertEquals(
            user.getEmail(),
            jwtService.getEmailFromToken(token)
        );

        assertEquals(
            user.getId(),
            jwtService.getUserIdFromToken(token)
        );

        assertEquals(
            user.getRole(),
            jwtService.getUserRoleFromToken(token)
        );
    }

    @Test
    void validateToken_shouldThrow_whenTokenIsInvalid() {
        assertThrows(
            JwtValidationException.class,
            () -> jwtService.validateAndExtractClaims(TestConstants.INVALID_REFRESH_TOKEN)
        );
    }

    @Test
    void validateAndExtractClaims_shouldThrow_whenTokenIsExpired() {

        ReflectionTestUtils.setField(jwtService, "expiration", -1L);

        String token = jwtService.generateToken(user);


        assertThrows(
            JwtValidationException.class,
            () -> jwtService.validateAndExtractClaims(token)
        );
    }

    @Test
    void validateAndExtractClaims_shouldThrow_whenIssuerIsInvalid() {
        ReflectionTestUtils.setField(jwtService, "issuer", "auth-service");

        String token = jwtService.generateToken(user);

        ReflectionTestUtils.setField(jwtService, "issuer", "another-issuer");

        assertThrows(
            JwtValidationException.class,
            () -> jwtService.validateAndExtractClaims(token)
        );
    }

    @Test
    void getUserRoleFromToken_shouldThrow_whenRoleIsInvalid() {

        ReflectionTestUtils.setField(jwtService, "issuer", "auth-service");

        String token = Jwts.builder()
            .setSubject(user.getEmail())
            .claim("userId", user.getId())
            .claim("role", "INVALID_ROLE")
            .setIssuer("auth-service")
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 3600000))
            .signWith(
                Keys.hmacShaKeyFor(
                    "testSecretKeytestSecretKeytestSecretKey"
                        .getBytes(StandardCharsets.UTF_8)
                )
            )
            .compact();

        assertThrows(
            InvalidTokenException.class,
            () -> jwtService.getUserRoleFromToken(token)
        );
    }
}

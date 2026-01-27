package com.quickbite.auth_service.service;

import com.quickbite.auth_service.constants.TestConstants;
import com.quickbite.auth_service.entity.User;
import com.quickbite.core.exception.JwtValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

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
            .role(User.UserRole.CUSTOMER)
            .build();
    }

    @Test
    void generateToken_shouldGenerateValidToken() {
        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertDoesNotThrow(() -> jwtService.validateToken(token));
    }

    @Test
    void validateToken_shouldThrow_whenTokenIsInvalid() {
        assertThrows(
            JwtValidationException.class,
            () -> jwtService.validateToken("invalid.token")
        );
    }
}

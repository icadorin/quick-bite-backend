package com.quickbite.auth_service.repository;

import com.quickbite.auth_service.constants.TestConstants;
import com.quickbite.auth_service.entity.RefreshToken;
import com.quickbite.auth_service.entity.User;
import com.quickbite.core.security.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findById_shouldReturnToken_whenTokenExists() {
        User user = userRepository.save(
            User.builder()
                .email(TestConstants.VALID_EMAIL)
                .fullName(TestConstants.VALID_FULL_NAME)
                .status(User.UserStatus.ACTIVE)
                .role(UserRole.CUSTOMER)
                .passwordHash("hashed")
                .build()
        );

        RefreshToken token = RefreshToken.builder()
            .token(TestConstants.VALID_REFRESH_TOKEN)
            .user(user)
            .expiresAt(LocalDateTime.now().plusDays(1))
            .revoked(false)
            .build();

        refreshTokenRepository.save(token);

        Optional<RefreshToken> result =
            refreshTokenRepository.findByToken(TestConstants.VALID_REFRESH_TOKEN);

        assertTrue(result.isPresent());
        assertEquals(TestConstants.VALID_REFRESH_TOKEN, result.get().getToken());
    }
}

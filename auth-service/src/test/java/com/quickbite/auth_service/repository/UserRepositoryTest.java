package com.quickbite.auth_service.repository;

import com.quickbite.auth_service.constants.TestConstants;
import com.quickbite.auth_service.entity.User;
import com.quickbite.core.security.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User createUser(String email, User.UserStatus status, UserRole role) {
        return User.builder()
            .email(email)
            .fullName(TestConstants.VALID_FULL_NAME)
            .passwordHash("hashed")
            .status(status)
            .role(role)
            .build();
    }

    private void saveUser(String email, User.UserStatus status, UserRole role) {
        userRepository.save(createUser(email, status, role));
    }

    @Test
    void findByEmail_shouldReturnUser_whenEmailExists() {
        saveUser(TestConstants.VALID_EMAIL, User.UserStatus.ACTIVE, UserRole.CUSTOMER);

        Optional<User> result =
            userRepository.findByEmail(TestConstants.VALID_EMAIL);

        assertTrue(result.isPresent());
        assertEquals(TestConstants.VALID_EMAIL, result.get().getEmail());
    }

    @Test
    void findByEmail_shouldReturnEmpty_whenEmailDoesNotExist() {
        Optional<User> result =
            userRepository.findByEmail(TestConstants.NON_EXISTENT_EMAIL);

        assertTrue(result.isEmpty());
    }

    @Test
    void existsByEmailIgnoreCase_shouldReturnTrue_whenEmailExistsIgnoringCase() {
        saveUser(TestConstants.VALID_EMAIL, User.UserStatus.ACTIVE, UserRole.CUSTOMER);

        assertTrue(userRepository.existsByEmailIgnoreCase(TestConstants.VALID_EMAIL.toLowerCase()));
        assertTrue(userRepository.existsByEmailIgnoreCase(TestConstants.VALID_EMAIL.toUpperCase()));
    }

    @ParameterizedTest
    @EnumSource(User.UserStatus.class)
    void findByStatus_shouldWorkForAllStatuses(User.UserStatus status) {
        saveUser(
            "status_" + status.name().toLowerCase() + "@test.com",
            status,
            UserRole.CUSTOMER
        );

        List<User> result = userRepository.findByStatus(status);
        assertFalse(result.isEmpty());
    }

    @ParameterizedTest
    @EnumSource(UserRole.class)
    void findByRole_shouldWorkForAllRoles(UserRole role) {
        saveUser(
            "role_" + role.name().toLowerCase() + "@test.com",
            User.UserStatus.ACTIVE,
            role
        );

        List<User> result = userRepository.findByRole(role);
        assertFalse(result.isEmpty());
    }
}

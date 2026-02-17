package com.quickbite.auth_service.repository;

import com.quickbite.auth_service.constants.TestConstants;
import com.quickbite.auth_service.entity.User;
import com.quickbite.core.security.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User createUser() {
        return User.builder()
            .email(TestConstants.VALID_EMAIL)
            .fullName(TestConstants.VALID_FULL_NAME)
            .passwordHash("hashed")
            .status(User.UserStatus.ACTIVE)
            .role(UserRole.CUSTOMER)
            .build();
    }

    private void saveUser() {
        userRepository.save(createUser(
        ));
    }

    @Test
    void findByEmail_shouldReturnUser_whenEmailExists() {
        saveUser();

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
        saveUser();

        assertTrue(userRepository
            .existsByEmailIgnoreCase(TestConstants.VALID_EMAIL.toLowerCase()));
        assertTrue(userRepository
            .existsByEmailIgnoreCase(TestConstants.VALID_EMAIL.toUpperCase()));
    }
}

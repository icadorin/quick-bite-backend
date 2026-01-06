package com.quickbite.auth_service.repository;

import com.quickbite.auth_service.constants.TestConstants;
import com.quickbite.auth_service.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User createActiveCustomer() {
        return User.builder()
            .email(TestConstants.VALID_EMAIL)
            .fullName(TestConstants.VALID_FULL_NAME)
            .passwordHash("hashed")
            .status(User.UserStatus.ACTIVE)
            .role(User.UserRole.CUSTOMER)
            .build();
    }

    @Test
    void findByEmail_shouldReturnUser_whenEmailExists() {
        userRepository.save(createActiveCustomer());

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
    void existsByEmail_shouldReturnTrue_whenEmailExists() {
        userRepository.save(createActiveCustomer());

        boolean exists =
            userRepository.existsByEmail(TestConstants.VALID_EMAIL);

        assertTrue(exists);
    }

    @Test
    void existsByEmail_shouldReturnFalse_whenEmailDoesNotExist() {
        boolean exists =
            userRepository.existsByEmail(TestConstants.VALID_EMAIL);

        assertFalse(exists);
    }

    @Test
    void existsByEmail_shouldReturnTrue_whenEmailExistsIgnoringCase() {
        userRepository.save(createActiveCustomer());

        boolean exists =
            userRepository.existsByEmail(
                TestConstants.VALID_EMAIL.toLowerCase()
            );

        assertTrue(exists);
    }

    @Test
    void findByStatus_shouldReturnUserWithGivenStatus() {
        userRepository.save(createActiveCustomer());

        List<User> result =
            userRepository.findByStatus(User.UserStatus.ACTIVE);

        assertEquals(1, result.size());
    }

    @Test
    void findByRole_shouldReturnUserWithGivenRole() {
        userRepository.save(createActiveCustomer());

        List<User> result =
            userRepository.findByRole(User.UserRole.CUSTOMER);

        assertEquals(1, result.size());
    }
}

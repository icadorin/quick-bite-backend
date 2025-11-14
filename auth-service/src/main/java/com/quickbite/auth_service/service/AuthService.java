package com.quickbite.auth_service.service;

import com.quickbite.auth_service.constants.AuthConstants;
import com.quickbite.auth_service.dto.AuthResponse;
import com.quickbite.auth_service.dto.RegisterRequest;
import com.quickbite.auth_service.entity.RefreshToken;
import com.quickbite.auth_service.entity.User;
import com.quickbite.auth_service.entity.UserProfile;
import com.quickbite.auth_service.exception.*;
import com.quickbite.auth_service.repository.RefreshTokenRepository;
import com.quickbite.auth_service.repository.UserProfileRepository;
import com.quickbite.auth_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registrando novo usuário: {}", request.getEmail());

        validateRegistrationRequest(request);
        validateEmailNotExists(request.getEmail());

        User user = createUser(request);
        createUserProfile(user, request);

        String accessToken = jwtService.generateToken(user);
        String refreshToken = generateRefreshToken(user);

        log.info("Usuário registrado com sucesso: {}", user.getEmail());
        return buildAuthResponse(accessToken, refreshToken, user);
    }

    public AuthResponse login(String email, String password) {
        log.info("Tentativa de login: {}", email);

        try {
            validateEmailFormat(email);
            validatePasswordNotEmpty(password);

            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
            );

            User user = (User) authentication.getPrincipal();
            validateUserActive(user);

            String accessToken = jwtService.generateToken(user);
            String refreshToken = generateRefreshToken(user);

            log.info("Login realizado com sucesso: {}", email);
            return new AuthResponse(accessToken, refreshToken, user);
        } catch (BadCredentialsException e) {
            log.warn("Credenciais inválidas para: {}", email);
            throw new AuthException(AuthConstants.INVALID_CREDENTIALS);
        } catch (DisabledException e) {
            log.warn("Usuário desativado tentou login: {}", email);
            throw new InvalidUserStatusException(AuthConstants.USER_DISABLED);
        } catch (AuthenticationException e) {
            log.warn("Falha na autenticação para: {} - {}", email, e.getMessage());
            throw new AuthException(AuthConstants.AUTHENTICATION_ERROR);
        }
    }

    private void validateRegistrationRequest(RegisterRequest request) {
        validateEmailFormat(request.getEmail());
        validatePasswordStrength(request.getPassword());
        validateFullName(request.getFullName());

        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            validatePhoneFormat(request.getPhone());
        }
    }

    private void validateEmailFormat(String email){
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException(AuthConstants.EMAIL_REQUIRED);
        }

        if (!AuthConstants.EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException(AuthConstants.INVALID_EMAIL_FORMAT);
        }
    }

    private void validatePasswordStrength(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException(AuthConstants.PASSWORD_REQUIRED);
        }

        if (password.length() < AuthConstants.MIN_PASSWORD_LENGTH) {
            throw new ValidationException(AuthConstants.PASSWORD_TOO_SHORT);
        }
    }

    private void validatePasswordNotEmpty(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException(AuthConstants.PASSWORD_REQUIRED);
        }
    }

    private void validateFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new ValidationException(AuthConstants.FULL_NAME_REQUIRED);
        }

        if (fullName.trim().length() < 2) {
            throw new ValidationException(AuthConstants.FULL_NAME_TOO_SHORT);
        }
    }

    private void validatePhoneFormat(String phone) {
        String clearProne = phone.replaceAll("[^0-9]", "");

        if (clearProne.length() < 10 || clearProne.length() > 11) {
            throw new ValidationException(AuthConstants.INVALID_PHONE);
        }
    }

    private void validateEmailNotExists(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            log.warn("Tentativa de registrar email já existente: {}", email);
            throw new UserAlreadyExistsException(AuthConstants.USER_ALREADY_EXISTS);
        }
    }

    private User createUser(RegisterRequest request) {
        User user = User.builder()
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .fullName(request.getFullName())
            .build();

        return userRepository.save(user);
    }

    public void createUserProfile(User user, RegisterRequest request) {
        UserProfile profile = UserProfile.builder()
            .user(user)
            .phone(request.getPhone())
            .address(request.getAddress())
            .build();

        userProfileRepository.save(profile);
    }

    private User.UserRole parseUserRole (String role) {
        try {
            return User.UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AuthException(AuthConstants.INVALID_ROLE + role);
        }
    }

    private void validateUserActive(User user) {
        if(user.getStatus() != User.UserStatus.ACTIVE) {
            throw new InvalidUserStatusException(AuthConstants.INVALID_USER_STATUS);
        }
    }

    private String generateRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
            .user(user)
            .token(UUID.randomUUID().toString())
            .expiresAt(LocalDateTime.now().plusDays(AuthConstants.REFRESH_TOKEN_EXPIRATION_DAYS))
            .revoked(false)
            .build();

        refreshTokenRepository.save(refreshToken);

        return refreshToken.getToken();
    }

    @Transactional
    public  AuthResponse refreshToken(String refreshToken) {
        log.info("Atualizando token com refresh token");

        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new TokenException(AuthConstants.REFRESH_TOKEN_NOT_PROVIDED);
        }

        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
            .orElseThrow(() -> new AuthException(AuthConstants.INVALID_REFRESH_TOKEN));

        validateRefreshToken(storedToken);

        User user = storedToken.getUser();
        validateUserActive(user);

        revokeRefreshToken(storedToken);

        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = generateRefreshToken(user);

        log.info("Token atualizado com sucesso para usuário: {}", user.getEmail());
        return new AuthResponse(newAccessToken, newRefreshToken, user);
    }

    private AuthResponse buildAuthResponse(String accessToken, String refreshToken, User user) {
        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(jwtService.getTokenExpirationInSeconds())
            .user(user)
            .build();
    }

    private void validateRefreshToken(RefreshToken refreshToken) {
        if (refreshToken.isRevoked()) {
            throw new AuthException(AuthConstants.REFRESH_TOKEN_REVOKED);
        }

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            throw new AuthException(AuthConstants.REFRESH_TOKEN_EXPIRED);
        }
    }

    private void revokeRefreshToken(RefreshToken refreshToken) {
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }
}

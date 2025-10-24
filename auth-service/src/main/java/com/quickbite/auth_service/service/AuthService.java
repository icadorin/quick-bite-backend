package com.quickbite.auth_service.service;

import com.quickbite.auth_service.dto.AuthResponse;
import com.quickbite.auth_service.dto.RegisterRequest;
import com.quickbite.auth_service.entity.RefreshToken;
import com.quickbite.auth_service.entity.User;
import com.quickbite.auth_service.entity.UserProfile;
import com.quickbite.auth_service.exception.AuthException;
import com.quickbite.auth_service.repository.RefreshTokenRepository;
import com.quickbite.auth_service.repository.UserProfileRepository;
import com.quickbite.auth_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
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

    private static final int REFRESH_TOKEN_EXPIRATION_DAYS = 7;

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registrando novo usuário: {}", request.getEmail());

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
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
            );

            User user = (User) authentication.getPrincipal();
            validateUserActive(user);

            String accessToken = jwtService.generateToken(user);
            String refreshToken = generateRefreshToken(user);

            log.info("Login realizado com sucesso: {}", email);
            return new AuthResponse(accessToken, refreshToken, user);
        } catch (AuthenticationException e) {
            log.warn("Falha na autenticação para: {} - {}", email, e.getMessage());
            throw new AuthException("Credenciais inválidas");
        }
    }

    private void validateEmailNotExists(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new AuthException("Email já cadastrado");
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
            throw new AuthException("Role inválida: " + role);
        }
    }

    private void validateUserActive(User user) {
        if(user.getStatus() != User.UserStatus.ACTIVE) {
            throw new AuthException("Usuário inativo");
        }
    }

    private String generateRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
            .user(user)
            .token(UUID.randomUUID().toString())
            .expiresAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_EXPIRATION_DAYS))
            .revoked(false)
            .build();

        refreshTokenRepository.save(refreshToken);

        return refreshToken.getToken();
    }

    @Transactional
    public  AuthResponse refreshToken(String refreshToken) {
        log.info("Atualizando token com refresh token");

        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
            .orElseThrow(() -> new AuthException("Refresh token inválido"));

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
            throw new AuthException("Refresh token revogado");
        }

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            throw new AuthException("Refresh token expirado");
        }
    }

    private void revokeRefreshToken(RefreshToken refreshToken) {
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }
}

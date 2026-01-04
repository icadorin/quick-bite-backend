package com.quickbite.auth_service.service;

import com.quickbite.auth_service.dto.LoginRequest;
import com.quickbite.auth_service.dto.LoginResponse;
import com.quickbite.auth_service.dto.RegisterRequest;
import com.quickbite.auth_service.entity.RefreshToken;
import com.quickbite.auth_service.entity.User;
import com.quickbite.auth_service.entity.UserProfile;
import com.quickbite.auth_service.exception.*;
import com.quickbite.auth_service.mapper.UserCreateMapper;
import com.quickbite.auth_service.mapper.UserResponseMapper;
import com.quickbite.auth_service.repository.RefreshTokenRepository;
import com.quickbite.auth_service.repository.UserProfileRepository;
import com.quickbite.auth_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${auth.refresh-token.expiration-days}")
    private int refreshTokenExpirationDays;

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserCreateMapper userCreateMapper;
    private final UserResponseMapper userResponseMapper;

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        validateEmailNotExists(request.getEmail());

        User user = createUser(request);
        createUserProfile(user, request);

        return generateLoginResponse(user);
    }

    public LoginResponse login(LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );

        User user = (User) authentication.getPrincipal();
        validateUserActive(user);

        return generateLoginResponse(user);
    }

    @Transactional
    public  LoginResponse refreshToken(String refreshToken) {

        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
            .orElseThrow(() -> new AuthException("Invalid token"));

        validateRefreshToken(storedToken);

        User user = storedToken.getUser();
        validateUserActive(user);

        revokeRefreshToken(storedToken);

        return generateLoginResponse(user);
    }

    private void validateEmailNotExists(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException(
                "User already exists"
            );
        }
    }

    private void validateUserActive(User user) {
        if(user.getStatus() != User.UserStatus.ACTIVE) {
            throw new InvalidUserStatusException(
                "Invalid user status"
            );
        }
    }

    private void validateRefreshToken(RefreshToken refreshToken) {
        if (refreshToken.isRevoked()) {
            throw new AuthException(
                "Revoked token"
            );
        }

        if (refreshToken.isExpired()) {
            revokeRefreshToken(refreshToken);
            throw new AuthException("Expired token");
        }
    }

    private User createUser(RegisterRequest request) {
        User user = userCreateMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        return userRepository.save(user);
    }

    private void createUserProfile(User user, RegisterRequest request) {
        UserProfile profile = UserProfile.builder()
            .user(user)
            .phone(request.getPhone())
            .address(request.getAddress())
            .build();

        userProfileRepository.save(profile);
    }

    private LoginResponse generateLoginResponse(User user) {
        String accessToken = jwtService.generateToken(user);
        String refreshToken = createRefreshToken(user);

        return LoginResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(jwtService.getTokenExpirationInSeconds())
            .user(userResponseMapper.toResponse(user))
            .build();
    }

    private String createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
            .user(user)
            .token(UUID.randomUUID().toString())
            .expiresAt(
                LocalDateTime.now()
                    .plusDays(refreshTokenExpirationDays)
            )
            .revoked(false)
            .build();

        refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
    }

    private void revokeRefreshToken(RefreshToken refreshToken) {
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }
}

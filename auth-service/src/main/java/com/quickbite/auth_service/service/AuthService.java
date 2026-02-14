package com.quickbite.auth_service.service;

import com.quickbite.auth_service.dto.LoginRequest;
import com.quickbite.auth_service.dto.LoginResponse;
import com.quickbite.auth_service.dto.RegisterRequest;
import com.quickbite.auth_service.entity.RefreshToken;
import com.quickbite.auth_service.entity.User;
import com.quickbite.auth_service.entity.UserProfile;
import com.quickbite.auth_service.mapper.UserCreateMapper;
import com.quickbite.auth_service.mapper.UserResponseMapper;
import com.quickbite.auth_service.repository.RefreshTokenRepository;
import com.quickbite.auth_service.repository.UserProfileRepository;
import com.quickbite.auth_service.repository.UserRepository;
import com.quickbite.core.exception.InvalidUserStatusException;
import com.quickbite.core.exception.TokenException;
import com.quickbite.core.exception.UserAlreadyExistsException;
import com.quickbite.core.security.UserRole;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Validated
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
    public LoginResponse register(@Valid RegisterRequest request) {
        validateEmailNotExists(request.getEmail());

        User user = createUser(request);
        createUserProfile(user, request);

        return generateLoginResponse(user);
    }

    public LoginResponse login(@Valid LoginRequest request) {
        Authentication authentication = authenticate(request);
        User user = (User) authentication.getPrincipal();

        validateUserActive(user);

        return generateLoginResponse(user);
    }

    @Transactional
    public  LoginResponse refreshToken(String token) {
        validateRequiredToken(token);

        RefreshToken storedToken = refreshTokenRepository.findByToken(token)
            .orElseThrow(() -> new TokenException("Invalid refresh token"));

        validateRefreshToken(storedToken);

        User user = storedToken.getUser();
        validateUserActive(user);

        revokeRefreshToken(storedToken);

        return generateLoginResponse(user);
    }

    private void validateEmailNotExists(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("User already exists");
        }
    }

    private void validateUserActive(User user) {
        if(user.getStatus() != User.UserStatus.ACTIVE) {
            throw new InvalidUserStatusException("User is not active");
        }
    }

    private void validateRefreshToken(RefreshToken refreshToken) {
        if (refreshToken.isRevoked()) {
            throw new TokenException("Revoked token revoked");
        }

        if (refreshToken.isExpired()) {
            revokeRefreshToken(refreshToken);
            throw new TokenException("Refresh token expired");
        }
    }

    private void validateRequiredToken(String token) {
        if (token == null || token.isBlank()) {
            throw new TokenException("Refresh token must not be blank");
        }
    }

    private Authentication authenticate(LoginRequest request) {
        return authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );
    }

    private User createUser(RegisterRequest request) {
        User user = userCreateMapper.toEntity(request);

        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.CUSTOMER);
        user.setStatus(User.UserStatus.ACTIVE);

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

    public void logout(String token) {

        if (token == null || token.isBlank()) {
            throw new TokenException("Refresh token must not be blank");
        }

        RefreshToken storedToken =  refreshTokenRepository.findByToken(token)
            .orElseThrow(() -> new TokenException("Invalid refresh token"));

        Long userId = storedToken.getUser().getId();

        refreshTokenRepository.
            findAllByUser_IdAndRevokedFalse(userId)
            .forEach(rt -> {
                rt.setRevoked(true);
                refreshTokenRepository.save(rt);
            });
    }

    private void revokeRefreshToken(RefreshToken refreshToken) {
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }
}

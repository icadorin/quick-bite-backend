package com.quickbite.auth_service.dto;

import com.quickbite.auth_service.entity.User;
import lombok.Data;

@Data
public class AuthResponse {
    private String acessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private User user;

    public AuthResponse(String acessToken, String refreshToken, User user) {
        this.acessToken = acessToken;
        this.refreshToken = refreshToken;
        this.user = user;
        this.expiresIn = 3600L;
    }
}

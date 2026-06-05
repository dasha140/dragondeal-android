package com.elvsf.dragondeal.dto;

import java.math.BigDecimal;

public class AuthResponse {

    private Long id;
    private String displayName;
    private String username;
    private String email;
    private String avatarUrl;
    private BigDecimal rating;
    private String role;
    private String createdAt;
    private String accessToken;

    public Long getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public String getRole() {
        return role;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getNameForDisplay() {
        if (displayName != null && !displayName.trim().isEmpty()) {
            return displayName;
        }

        if (username != null && !username.trim().isEmpty()) {
            return username;
        }

        return "Пользователь";
    }
}
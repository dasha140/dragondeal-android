package com.elvsf.dragondeal.dto;

import java.math.BigDecimal;

public class UserResponse {

    private Long id;
    private String displayName;
    private String username;
    private String avatarUrl;
    private BigDecimal rating;
    private Long completedOrdersCount;
    private String role;
    private String createdAt;

    public Long getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUsername() {
        return username;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public Long getCompletedOrdersCount() {
        return completedOrdersCount;
    }

    public String getRole() {
        return role;
    }

    public String getCreatedAt() {
        return createdAt;
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
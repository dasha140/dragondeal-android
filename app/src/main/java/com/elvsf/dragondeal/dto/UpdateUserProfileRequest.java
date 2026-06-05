package com.elvsf.dragondeal.dto;

public class UpdateUserProfileRequest {

    private String displayName;
    private String avatarUrl;

    public UpdateUserProfileRequest(String displayName, String avatarUrl) {
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }
}
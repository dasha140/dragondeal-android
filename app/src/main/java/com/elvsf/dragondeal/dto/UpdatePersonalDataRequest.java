package com.elvsf.dragondeal.dto;

public class UpdatePersonalDataRequest {

    private String displayName;
    private String email;

    public UpdatePersonalDataRequest(String displayName, String email) {
        this.displayName = displayName;
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }
}
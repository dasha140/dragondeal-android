package com.elvsf.dragondeal.dto;

public class RequestEmailChangeRequest {

    private String newEmail;
    private String currentPassword;

    public RequestEmailChangeRequest(String newEmail, String currentPassword) {
        this.newEmail = newEmail;
        this.currentPassword = currentPassword;
    }

    public String getNewEmail() {
        return newEmail;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }
}
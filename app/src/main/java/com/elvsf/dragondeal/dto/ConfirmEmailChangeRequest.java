package com.elvsf.dragondeal.dto;

public class ConfirmEmailChangeRequest {

    private String newEmail;
    private String code;

    public ConfirmEmailChangeRequest(String newEmail, String code) {
        this.newEmail = newEmail;
        this.code = code;
    }

    public String getNewEmail() {
        return newEmail;
    }

    public String getCode() {
        return code;
    }
}
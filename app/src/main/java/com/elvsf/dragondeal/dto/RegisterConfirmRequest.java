package com.elvsf.dragondeal.dto;

public class RegisterConfirmRequest {

    private String email;
    private String code;

    public RegisterConfirmRequest(String email, String code) {
        this.email = email;
        this.code = code;
    }

    public String getEmail() {
        return email;
    }

    public String getCode() {
        return code;
    }
}

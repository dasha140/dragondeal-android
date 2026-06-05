package com.elvsf.dragondeal.dto;

public class FirebaseLoginRequest {

    private String idToken;

    public FirebaseLoginRequest(String idToken) {
        this.idToken = idToken;
    }

    public String getIdToken() {
        return idToken;
    }
}
package com.elvsf.dragondeal.storage;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenStorage {

    private static final String PREF_NAME = "dragondeal_auth";

    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ROLE = "role";

    private final SharedPreferences preferences;

    public TokenStorage(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveAuthData(
            String accessToken,
            Long userId,
            String username,
            String email,
            String role
    ) {
        preferences.edit()
                .putString(KEY_ACCESS_TOKEN, accessToken == null ? "" : accessToken)
                .putLong(KEY_USER_ID, userId == null ? 0L : userId)
                .putString(KEY_USERNAME, username == null ? "" : username)
                .putString(KEY_EMAIL, email == null ? "" : email)
                .putString(KEY_ROLE, role == null ? "" : role)
                .apply();
    }

    public String getAccessToken() {
        return preferences.getString(KEY_ACCESS_TOKEN, "");
    }

    public Long getUserId() {
        return preferences.getLong(KEY_USER_ID, 0L);
    }

    public String getUsername() {
        return preferences.getString(KEY_USERNAME, "");
    }

    public String getEmail() {
        return preferences.getString(KEY_EMAIL, "");
    }

    public String getRole() {
        return preferences.getString(KEY_ROLE, "");
    }

    public boolean isLoggedIn() {
        String token = getAccessToken();
        return token != null && !token.trim().isEmpty();
    }

    public void clear() {
        preferences.edit().clear().apply();
    }
}
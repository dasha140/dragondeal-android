package com.elvsf.dragondeal.util;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.elvsf.dragondeal.screens.SigninActivity;
import com.elvsf.dragondeal.storage.TokenStorage;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Base Callback that handles:
 * - Network errors (via BackendErrorParser)
 * - Auth errors (auto-redirect to login)
 * - Successful responses (delegated to onSuccess)
 *
 * Usage:
 *   api.call().enqueue(new ApiCallback<MyResponse>(this) {
 *       @Override
 *       protected void onSuccess(Response<MyResponse> response) { ... }
 *   });
 */
public abstract class ApiCallback<T> implements Callback<T> {

    private final Context context;

    public ApiCallback(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
        if (response.isSuccessful() && response.body() != null) {
            onSuccess(response);
            return;
        }

        // Check for auth errors
        if (isAuthError(response)) {
            handleAuthError();
            return;
        }

        // Handle other HTTP errors
        String message = BackendErrorParser.parse(response);
        showToast(message);
    }

    @Override
    public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
        // Check for auth-related failures
        String msg = t.getMessage();
        if (msg != null && isAuthMessage(msg)) {
            handleAuthError();
            return;
        }

        String message = BackendErrorParser.parse(t);
        showToast(message);
    }

    /**
     * Called when the response is successful (2xx) and body is non-null.
     */
    protected abstract void onSuccess(Response<T> response);

    /**
     * Override to customize error display (e.g., use a Dialog instead of Toast).
     */
    protected void showError(String message) {
        showToast(message);
    }

    /* --------------- private helpers --------------- */

    private boolean isAuthError(Response<T> response) {
        int code = response.code();
        if (code == 401 || code == 403) {
            return true;
        }
        if (code == 400 && response.errorBody() != null) {
            try {
                String raw = response.errorBody().string();
                return raw != null && isAuthMessage(raw);
            } catch (Exception ignored) {
                return false;
            }
        }
        return false;
    }

    private boolean isAuthMessage(String message) {
        if (message == null) return false;
        String lower = message.toLowerCase();
        return lower.contains("user not found")
                || lower.contains("invalid or expired token")
                || lower.contains("jwt")
                || lower.contains("unauthorized")
                || lower.contains("authorization token is required");
    }

    private void handleAuthError() {
        new TokenStorage(context).clear();
        Intent intent = new Intent(context, SigninActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        showToast("Сессия истекла. Войдите снова.");
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}

package com.elvsf.dragondeal.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import com.elvsf.dragondeal.ui.DarkDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.elvsf.dragondeal.api.AuthApi;
import com.elvsf.dragondeal.api.RetrofitClient;
import com.elvsf.dragondeal.databinding.ActivitySigninBinding;
import com.elvsf.dragondeal.dto.AuthResponse;
import com.elvsf.dragondeal.dto.LoginRequest;
import com.elvsf.dragondeal.storage.TokenStorage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SigninActivity extends AppCompatActivity {

    private ActivitySigninBinding binding;
    private AuthApi authApi;
    private TokenStorage tokenStorage;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySigninBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authApi = RetrofitClient.getInstance(this).create(AuthApi.class);
        tokenStorage = new TokenStorage(this);
        firebaseAuth = FirebaseAuth.getInstance();

        if (tokenStorage.isLoggedIn()) {
            openMainScreen();
            return;
        }

        setupClicks();
    }

    private void setupClicks() {
        binding.signin.setOnClickListener(view -> loginWithFirebase());

        binding.signup.setOnClickListener(view -> {
            Intent intent = new Intent(SigninActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        binding.textForgotPassword.setOnClickListener(view -> showResetPasswordDialog());
    }

    private void loginWithFirebase() {
        String email = binding.email.getText().toString().trim();
        String password = binding.password.getText().toString().trim();

        if (email.isEmpty()) {
            binding.email.setError("Введи email");
            return;
        }

        if (password.isEmpty()) {
            binding.password.setError("Введи пароль");
            return;
        }

        setLoading(true, "Проверяем...");

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        setLoading(false, "Войти");

                        String message = "Неверный email или пароль";

                        if (task.getException() != null && task.getException().getMessage() != null) {
                            message = task.getException().getMessage();
                        }

                        showErrorDialog("Ошибка входа", message);
                        return;
                    }

                    FirebaseUser user = firebaseAuth.getCurrentUser();

                    if (user == null) {
                        setLoading(false, "Войти");
                        showToast("Firebase-пользователь не найден");
                        return;
                    }

                    user.reload().addOnCompleteListener(reloadTask -> {
                        FirebaseUser updatedUser = firebaseAuth.getCurrentUser();

                        if (updatedUser == null) {
                            setLoading(false, "Войти");
                            showToast("Firebase-пользователь не найден");
                            return;
                        }

                        if (!updatedUser.isEmailVerified()) {
                            setLoading(false, "Войти");
                            showEmailNotVerifiedDialog(updatedUser);
                            return;
                        }

                        loginInBackendWithFirebaseToken(updatedUser);
                    });
                });
    }

    private void loginInBackendWithFirebaseToken(FirebaseUser firebaseUser) {
        firebaseUser.getIdToken(true)
                .addOnCompleteListener(tokenTask -> {
                    if (!tokenTask.isSuccessful() || tokenTask.getResult() == null) {
                        setLoading(false, "Войти");
                        showToast("Не удалось получить Firebase token");
                        return;
                    }

                    String idToken = tokenTask.getResult().getToken();

                    authApi.firebaseLogin(new com.elvsf.dragondeal.dto.FirebaseLoginRequest(idToken))
                            .enqueue(new Callback<AuthResponse>() {
                                @Override
                                public void onResponse(
                                        @NonNull Call<AuthResponse> call,
                                        @NonNull Response<AuthResponse> response
                                ) {
                                    setLoading(false, "Войти");

                                    if (!response.isSuccessful() || response.body() == null) {
                                        String errorMessage = "Backend не выдал токен: " + response.code();

                                        try {
                                            if (response.errorBody() != null) {
                                                errorMessage = response.errorBody().string();
                                            }
                                        } catch (Exception ignored) {
                                        }

                                        showErrorDialog("Ошибка backend", errorMessage);
                                        return;
                                    }

                                    AuthResponse authResponse = response.body();

                                    tokenStorage.saveAuthData(
                                            authResponse.getAccessToken(),
                                            authResponse.getId(),
                                            authResponse.getUsername(),
                                            authResponse.getEmail(),
                                            authResponse.getRole()
                                    );

                                    showToast("Вход выполнен");
                                    openMainScreen();
                                }

                                @Override
                                public void onFailure(
                                        @NonNull Call<AuthResponse> call,
                                        @NonNull Throwable t
                                ) {
                                    setLoading(false, "Войти");
                                    showToast("Ошибка сети: " + t.getMessage());
                                }
                            });
                });
    }

    private void loginInBackend(String email, String password) {
        LoginRequest request = new LoginRequest(email, password);

        authApi.login(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<AuthResponse> call,
                    @NonNull Response<AuthResponse> response
            ) {
                setLoading(false, "Войти");

                if (!response.isSuccessful() || response.body() == null) {
                    String errorMessage = "Firebase вход выполнен, но backend не выдал токен: " + response.code();

                    try {
                        if (response.errorBody() != null) {
                            errorMessage = response.errorBody().string();
                        }
                    } catch (Exception ignored) {
                    }

                    showErrorDialog("Ошибка backend", errorMessage);
                    return;
                }

                AuthResponse authResponse = response.body();

                if (authResponse.getAccessToken() == null || authResponse.getAccessToken().trim().isEmpty()) {
                    showToast("Backend не вернул токен");
                    return;
                }

                tokenStorage.saveAuthData(
                        authResponse.getAccessToken(),
                        authResponse.getId(),
                        authResponse.getUsername(),
                        authResponse.getEmail(),
                        authResponse.getRole()
                );

                showToast("Вход выполнен");
                openMainScreen();
            }

            @Override
            public void onFailure(
                    @NonNull Call<AuthResponse> call,
                    @NonNull Throwable t
            ) {
                setLoading(false, "Войти");
                showToast("Ошибка сети: " + t.getMessage());
            }
        });
    }

    private void showEmailNotVerifiedDialog(FirebaseUser user) {
        DarkDialog.showConfirm(
                SigninActivity.this,
                "Email не подтверждён",
                "Сначала подтвердите email по ссылке в письме. Если письмо не пришло, можно отправить его ещё раз.",
                "Отправить ещё раз",
                "Закрыть",
                () -> resendVerificationEmail(user)
        );
    }

    private void resendVerificationEmail(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showToast("Письмо подтверждения отправлено");
                        return;
                    }

                    String message = "Не удалось отправить письмо";

                    if (task.getException() != null && task.getException().getMessage() != null) {
                        message = task.getException().getMessage();
                    }

                    showToast(message);
                });
    }

    private void showResetPasswordDialog() {
        String email = binding.email.getText().toString().trim();

        if (email.isEmpty()) {
            binding.email.setError("Введите email");
            showToast("Сначала введите email");
            return;
        }

        sendPasswordResetEmail(email);
    }

    private void sendPasswordResetEmail(String email) {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showToast("Письмо для смены пароля отправлено");
                        return;
                    }

                    String message = "Не удалось отправить письмо";

                    if (task.getException() != null && task.getException().getMessage() != null) {
                        message = task.getException().getMessage();
                    }

                    showToast(message);
                });
    }

    private void setLoading(boolean loading, String text) {
        binding.signin.setEnabled(!loading);
        binding.signin.setText(text);
    }

    private void showErrorDialog(String title, String message) {
        DarkDialog.showMessage(
                SigninActivity.this,
                title,
                message
        );
    }

    private void openMainScreen() {
        Intent intent = new Intent(SigninActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
package com.elvsf.dragondeal.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.elvsf.dragondeal.api.AuthApi;
import com.elvsf.dragondeal.api.RetrofitClient;
import com.elvsf.dragondeal.databinding.ActivitySignupBinding;
import com.elvsf.dragondeal.dto.MessageResponse;
import com.elvsf.dragondeal.dto.RegisterRequest;
import com.elvsf.dragondeal.util.BackendErrorParser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {

    public static final String EXTRA_DEV_CODE = "devCode";

    private ActivitySignupBinding binding;
    private AuthApi authApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authApi = RetrofitClient.getInstance(this).create(AuthApi.class);

        setupClicks();
    }

    private void setupClicks() {
        binding.btnCreateAccount.setOnClickListener(view -> requestRegistration());

        binding.tvGoToLogin.setOnClickListener(view -> {
            Intent intent = new Intent(SignupActivity.this, SigninActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void requestRegistration() {
        String username = binding.etUsername.getText().toString().trim();
        String email = binding.etRegisterEmail.getText().toString().trim();
        String password = binding.etRegisterPassword.getText().toString().trim();

        if (username.isEmpty()) {
            binding.etUsername.setError("Введи username");
            return;
        }

        if (email.isEmpty()) {
            binding.etRegisterEmail.setError("Введи email");
            return;
        }

        String passwordError = validatePassword(password);

        if (passwordError != null) {
            binding.etRegisterPassword.setError(passwordError);
            binding.textPasswordRules.setTextColor(android.graphics.Color.parseColor("#FF3B30"));
            showToast(passwordError);
            return;
        }

        binding.textPasswordRules.setTextColor(android.graphics.Color.parseColor("#8A8A8A"));

        setLoading(true, "Проверяем...");

        // Step 1: check username availability
        authApi.checkUsername(username).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<MessageResponse> call,
                    @NonNull Response<MessageResponse> response
            ) {
                if (!response.isSuccessful() || response.body() == null) {
                    setLoading(false, "Создать аккаунт");
                    showToast("Не удалось проверить username");
                    return;
                }

                if ("TAKEN".equals(response.body().getMessage())) {
                    setLoading(false, "Создать аккаунт");
                    binding.etUsername.setError("Этот username уже занят");
                    showToast("Этот username уже занят");
                    return;
                }

                // Step 2: check email availability
                checkEmailAvailability(username, email, password);
            }

            @Override
            public void onFailure(
                    @NonNull Call<MessageResponse> call,
                    @NonNull Throwable t
            ) {
                setLoading(false, "Создать аккаунт");
                showToast("Ошибка сети: " + t.getMessage());
            }
        });
    }

    private void checkEmailAvailability(String username, String email, String password) {
        authApi.checkEmail(email).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<MessageResponse> call,
                    @NonNull Response<MessageResponse> response
            ) {
                if (!response.isSuccessful() || response.body() == null) {
                    setLoading(false, "Создать аккаунт");
                    showToast("Не удалось проверить email");
                    return;
                }

                if ("TAKEN".equals(response.body().getMessage())) {
                    setLoading(false, "Создать аккаунт");
                    binding.etRegisterEmail.setError("Этот email уже занят");
                    showToast("Этот email уже занят");
                    return;
                }

                // Step 3: call register/request
                sendRegisterRequest(username, email, password);
            }

            @Override
            public void onFailure(
                    @NonNull Call<MessageResponse> call,
                    @NonNull Throwable t
            ) {
                setLoading(false, "Создать аккаунт");
                showToast("Ошибка сети: " + t.getMessage());
            }
        });
    }

    private void sendRegisterRequest(String username, String email, String password) {
        setLoading(true, "Отправляем код...");

        RegisterRequest request = new RegisterRequest(username, email, password);

        authApi.registerRequest(request).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<MessageResponse> call,
                    @NonNull Response<MessageResponse> response
            ) {
                if (!response.isSuccessful() || response.body() == null) {
                    setLoading(false, "Создать аккаунт");
                    String errorMessage = BackendErrorParser.parse(response);
                    showToast(errorMessage);
                    return;
                }

                MessageResponse body = response.body();
                String devCode = body.getDevCode();

                // Always open VerifyEmailActivity, pass devCode if available
                Intent intent = new Intent(SignupActivity.this, VerifyEmailActivity.class);
                intent.putExtra(VerifyEmailActivity.EXTRA_USERNAME, username);
                intent.putExtra(VerifyEmailActivity.EXTRA_EMAIL, email);
                intent.putExtra(VerifyEmailActivity.EXTRA_PASSWORD, password);
                if (devCode != null) {
                    intent.putExtra(EXTRA_DEV_CODE, devCode);
                }
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(
                    @NonNull Call<MessageResponse> call,
                    @NonNull Throwable t
            ) {
                setLoading(false, "Создать аккаунт");
                showToast("Ошибка сети: " + t.getMessage());
            }
        });
    }

    private String validatePassword(String password) {
        if (password == null || password.length() < 8) {
            return "Пароль должен быть не меньше 8 символов";
        }

        boolean hasLowercase = false;
        boolean hasUppercase = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);

            if (Character.isLowerCase(c)) {
                hasLowercase = true;
            } else if (Character.isUpperCase(c)) {
                hasUppercase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else {
                hasSpecial = true;
            }
        }

        if (!hasLowercase) {
            return "Пароль должен содержать строчную букву";
        }

        if (!hasUppercase) {
            return "Пароль должен содержать заглавную букву";
        }

        if (!hasDigit) {
            return "Пароль должен содержать цифру";
        }

        if (!hasSpecial) {
            return "Пароль должен содержать спецсимвол";
        }

        return null;
    }

    private void setLoading(boolean loading, String text) {
        binding.btnCreateAccount.setEnabled(!loading);
        binding.btnCreateAccount.setText(text);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

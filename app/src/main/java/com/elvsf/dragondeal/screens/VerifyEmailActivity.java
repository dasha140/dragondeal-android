package com.elvsf.dragondeal.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.elvsf.dragondeal.api.AuthApi;
import com.elvsf.dragondeal.api.RetrofitClient;
import com.elvsf.dragondeal.databinding.ActivityVerifyEmailBinding;
import com.elvsf.dragondeal.dto.AuthResponse;
import com.elvsf.dragondeal.dto.MessageResponse;
import com.elvsf.dragondeal.dto.RegisterConfirmRequest;
import com.elvsf.dragondeal.dto.RegisterRequest;
import com.elvsf.dragondeal.storage.TokenStorage;
import com.elvsf.dragondeal.util.BackendErrorParser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyEmailActivity extends AppCompatActivity {

    public static final String EXTRA_USERNAME = "username";
    public static final String EXTRA_EMAIL = "email";
    public static final String EXTRA_PASSWORD = "password";

    private ActivityVerifyEmailBinding binding;
    private AuthApi authApi;
    private TokenStorage tokenStorage;

    private String username;
    private String email;
    private String password;
    private String devCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityVerifyEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authApi = RetrofitClient.getInstance(this).create(AuthApi.class);
        tokenStorage = new TokenStorage(this);

        readIntentData();
        setupViews();
        setupClicks();
    }

    private void readIntentData() {
        username = getIntent().getStringExtra(EXTRA_USERNAME);
        email = getIntent().getStringExtra(EXTRA_EMAIL);
        password = getIntent().getStringExtra(EXTRA_PASSWORD);
        devCode = getIntent().getStringExtra(SignupActivity.EXTRA_DEV_CODE);

        if (username == null) username = "";
        if (email == null) email = "";
        if (password == null) password = "";
    }

    private void setupViews() {
        binding.textVerifyEmail.setText(email);

        // If devCode was passed, auto-fill the code field
        if (devCode != null) {
            binding.editVerificationCode.setText(devCode);
            Toast.makeText(
                    this,
                    "Dev mode: код подставлен автоматически",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void setupClicks() {
        binding.buttonCheckEmailVerified.setOnClickListener(view -> confirmRegistration());
        binding.buttonResendEmail.setOnClickListener(view -> resendCode());
        binding.buttonBackToSignup.setOnClickListener(view -> finish());
    }

    private void confirmRegistration() {
        String code = binding.editVerificationCode.getText().toString().trim();

        if (code.isEmpty()) {
            binding.editVerificationCode.setError("Введи код");
            return;
        }

        setLoading(true, "Проверяем...");

        RegisterConfirmRequest request = new RegisterConfirmRequest(email, code);

        authApi.registerConfirm(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<AuthResponse> call,
                    @NonNull Response<AuthResponse> response
            ) {
                setLoading(false, "Я подтвердил email");

                if (!response.isSuccessful() || response.body() == null) {
                    String errorMessage = BackendErrorParser.parse(response);
                    showToast(errorMessage);
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

                showToast("Email подтверждён, аккаунт создан");
                openMainScreen();
            }

            @Override
            public void onFailure(
                    @NonNull Call<AuthResponse> call,
                    @NonNull Throwable t
            ) {
                setLoading(false, "Я подтвердил email");
                showToast(BackendErrorParser.parse(t));
            }
        });
    }

    private void resendCode() {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showToast("Нет данных для повторной отправки. Вернись назад и попробуй снова.");
            return;
        }

        binding.buttonResendEmail.setEnabled(false);

        RegisterRequest request = new RegisterRequest(username, email, password);

        authApi.registerRequest(request).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<MessageResponse> call,
                    @NonNull Response<MessageResponse> response
            ) {
                binding.buttonResendEmail.setEnabled(true);

                if (!response.isSuccessful() || response.body() == null) {
                    String errorMessage = BackendErrorParser.parse(response);
                    showToast(errorMessage);
                    return;
                }

                MessageResponse body = response.body();
                String newDevCode = body.getDevCode();

                if (newDevCode != null) {
                    binding.editVerificationCode.setText(newDevCode);
                    Toast.makeText(
                            VerifyEmailActivity.this,
                            "Dev mode: новый код подставлен автоматически",
                            Toast.LENGTH_SHORT
                    ).show();
                } else {
                    Toast.makeText(
                            VerifyEmailActivity.this,
                            "Код отправлен на почту ещё раз",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<MessageResponse> call,
                    @NonNull Throwable t
            ) {
                binding.buttonResendEmail.setEnabled(true);
                showToast(BackendErrorParser.parse(t));
            }
        });
    }

    private void setLoading(boolean loading, String buttonText) {
        binding.buttonCheckEmailVerified.setEnabled(!loading);
        binding.buttonResendEmail.setEnabled(!loading);
        binding.buttonCheckEmailVerified.setText(buttonText);
    }

    private void openMainScreen() {
        Intent intent = new Intent(VerifyEmailActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

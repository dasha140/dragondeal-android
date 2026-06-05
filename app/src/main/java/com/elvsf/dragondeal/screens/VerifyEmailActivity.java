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
import com.elvsf.dragondeal.dto.RegisterRequest;
import com.elvsf.dragondeal.storage.TokenStorage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.elvsf.dragondeal.util.BackendErrorParser;

public class VerifyEmailActivity extends AppCompatActivity {

    public static final String EXTRA_USERNAME = "username";
    public static final String EXTRA_EMAIL = "email";
    public static final String EXTRA_PASSWORD = "password";

    private ActivityVerifyEmailBinding binding;
    private FirebaseAuth firebaseAuth;
    private AuthApi authApi;
    private TokenStorage tokenStorage;

    private String username;
    private String email;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityVerifyEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
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

        if (username == null) {
            username = "";
        }

        if (email == null) {
            email = "";
        }

        if (password == null) {
            password = "";
        }
    }

    private void setupViews() {
        binding.textVerifyEmail.setText(email);
    }

    private void setupClicks() {
        binding.buttonCheckEmailVerified.setOnClickListener(view -> checkEmailVerified());
        binding.buttonResendEmail.setOnClickListener(view -> resendEmail());
        binding.buttonBackToSignup.setOnClickListener(view -> finish());
    }

    private void checkEmailVerified() {
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user == null) {
            showToast("Firebase-пользователь не найден. Зарегистрируйся заново.");
            return;
        }

        setLoading(true, "Проверяем...");

        user.reload().addOnCompleteListener(task -> {
            FirebaseUser updatedUser = firebaseAuth.getCurrentUser();

            if (updatedUser == null) {
                setLoading(false, "Я подтвердил email");
                showToast("Firebase-пользователь не найден");
                return;
            }

            if (!updatedUser.isEmailVerified()) {
                setLoading(false, "Я подтвердил email");
                showToast("Email ещё не подтверждён");
                return;
            }

            registerInBackend();
        });
    }

    private void resendEmail() {
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user == null) {
            showToast("Firebase-пользователь не найден");
            return;
        }

        binding.buttonResendEmail.setEnabled(false);

        user.sendEmailVerification().addOnCompleteListener(task -> {
            binding.buttonResendEmail.setEnabled(true);

            if (!task.isSuccessful()) {
                String message = "Не удалось отправить письмо";

                if (task.getException() != null && task.getException().getMessage() != null) {
                    message = task.getException().getMessage();
                }

                showToast(message);
                return;
            }

            showToast("Письмо отправлено ещё раз");
        });
    }

    private void registerInBackend() {
        RegisterRequest request = new RegisterRequest(username, email, password);

        authApi.register(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<AuthResponse> call,
                    @NonNull Response<AuthResponse> response
            ) {
                setLoading(false, "Я подтвердил email");

                if (!response.isSuccessful() || response.body() == null) {
                    handleBackendError(response);
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

    private void handleBackendError(Response<AuthResponse> response) {
        String errorMessage = BackendErrorParser.parse(response);
        showToast(errorMessage);
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
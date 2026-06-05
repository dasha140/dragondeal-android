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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;
    private FirebaseAuth firebaseAuth;
    private AuthApi authApi;

    private String pendingUsername;
    private String pendingEmail;
    private String pendingPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        authApi = RetrofitClient.getInstance(this).create(AuthApi.class);

        setupClicks();
    }

    private void setupClicks() {
        binding.btnCreateAccount.setOnClickListener(view -> checkUsernameAndEmailBeforeFirebase());

        binding.tvGoToLogin.setOnClickListener(view -> {
            Intent intent = new Intent(SignupActivity.this, SigninActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void checkUsernameAndEmailBeforeFirebase() {
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

        pendingUsername = username;
        pendingEmail = email;
        pendingPassword = password;

        setLoading(true, "Проверяем...");

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

                checkEmailBeforeFirebase(email);
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

    private void checkEmailBeforeFirebase(String email) {
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

                startFirebaseRegistration();
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

    private void startFirebaseRegistration() {
        setLoading(true, "Создание...");

        firebaseAuth.createUserWithEmailAndPassword(pendingEmail, pendingPassword)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        setLoading(false, "Создать аккаунт");

                        String message = "Не удалось создать аккаунт";

                        if (task.getException() != null && task.getException().getMessage() != null) {
                            message = task.getException().getMessage();
                        }

                        if (message.contains("email address is already in use")
                                || message.contains("already in use")) {
                            binding.etRegisterEmail.setError("Этот email уже используется");
                            showToast("Этот email уже используется");
                            return;
                        }

                        if (message.contains("badly formatted")) {
                            binding.etRegisterEmail.setError("Некорректный email");
                            showToast("Проверь email");
                            return;
                        }

                        showToast(message);
                        return;
                    }

                    FirebaseUser user = firebaseAuth.getCurrentUser();

                    if (user == null) {
                        setLoading(false, "Создать аккаунт");
                        showToast("Firebase не вернул пользователя");
                        return;
                    }

                    sendVerificationEmail(user);
                });
    }

    private void tryLoginExistingFirebaseUser(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        setLoading(false, "Создать аккаунт");

                        String message = "Не удалось создать аккаунт";

                        if (task.getException() != null && task.getException().getMessage() != null) {
                            message = task.getException().getMessage();
                        }

                        if (message.contains("email address is already in use")
                                || message.contains("already in use")) {
                            binding.etRegisterEmail.setError("Этот email уже используется");
                            showToast("Этот email уже используется");
                            return;
                        }

                        showToast(message);
                        return;
                    }

                    FirebaseUser user = firebaseAuth.getCurrentUser();

                    if (user == null) {
                        setLoading(false, "Создать аккаунт");
                        showToast("Firebase-пользователь не найден");
                        return;
                    }

                    if (user.isEmailVerified()) {
                        setLoading(false, "Создать аккаунт");
                        openVerifyEmailScreen();
                    } else {
                        sendVerificationEmail(user);
                    }
                });
    }

    private void sendVerificationEmail(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    setLoading(false, "Создать аккаунт");

                    if (!task.isSuccessful()) {
                        String message = "Не удалось отправить письмо";

                        if (task.getException() != null && task.getException().getMessage() != null) {
                            message = task.getException().getMessage();
                        }

                        showToast(message);
                        return;
                    }

                    showToast("Письмо подтверждения отправлено");
                    openVerifyEmailScreen();
                });
    }

    private void openVerifyEmailScreen() {
        Intent intent = new Intent(SignupActivity.this, VerifyEmailActivity.class);
        intent.putExtra(VerifyEmailActivity.EXTRA_USERNAME, pendingUsername);
        intent.putExtra(VerifyEmailActivity.EXTRA_EMAIL, pendingEmail);
        intent.putExtra(VerifyEmailActivity.EXTRA_PASSWORD, pendingPassword);
        startActivity(intent);
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
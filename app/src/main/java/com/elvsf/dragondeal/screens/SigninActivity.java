package com.elvsf.dragondeal.screens;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.widget.Toast;

import com.elvsf.dragondeal.ui.DarkDialog;
import com.elvsf.dragondeal.R;
import androidx.appcompat.app.AppCompatActivity;

import com.elvsf.dragondeal.api.AuthApi;
import com.elvsf.dragondeal.databinding.ActivitySigninBinding;
import com.elvsf.dragondeal.databinding.DialogPasswordResetBinding;
import com.elvsf.dragondeal.dto.AuthResponse;
import com.elvsf.dragondeal.dto.LoginRequest;
import com.elvsf.dragondeal.dto.MessageResponse;
import com.elvsf.dragondeal.dto.PasswordResetConfirmRequest;
import com.elvsf.dragondeal.dto.PasswordResetRequest;
import com.elvsf.dragondeal.storage.TokenStorage;
import com.elvsf.dragondeal.util.ApiCallback;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.koin.java.KoinJavaComponent;

import static org.koin.java.KoinJavaComponent.get;

import retrofit2.Response;

public class SigninActivity extends AppCompatActivity {

    private ActivitySigninBinding binding;
    private AuthApi authApi;
    private TokenStorage tokenStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySigninBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authApi = get(AuthApi.class);
        tokenStorage = new TokenStorage(this);

        if (tokenStorage.isLoggedIn()) {
            openMainScreen();
            return;
        }

        setupClicks();
    }

    private void setupClicks() {
        binding.signin.setOnClickListener(view -> loginWithEmailPassword());

        binding.signup.setOnClickListener(view -> {
            Intent intent = new Intent(SigninActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        binding.textForgotPassword.setOnClickListener(view -> showResetPasswordDialog());

        setupPasswordToggle(binding.password, binding.btnTogglePassword);
    }

    private void setupPasswordToggle(
            android.widget.EditText editText,
            android.widget.ImageButton toggleButton
    ) {
        final boolean[] isVisible = {false};
        toggleButton.setOnClickListener(v -> {
            if (isVisible[0]) {
                editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                toggleButton.setImageResource(R.drawable.ic_eye_off);
                toggleButton.setContentDescription("Показать пароль");
            } else {
                editText.setTransformationMethod(null);
                toggleButton.setImageResource(R.drawable.ic_eye);
                toggleButton.setContentDescription("Скрыть пароль");
            }
            isVisible[0] = !isVisible[0];
            editText.setSelection(editText.getText().length());
        });
    }

    private void loginWithEmailPassword() {
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

        setLoading(true, "Входим...");

        LoginRequest request = new LoginRequest(email, password);

        authApi.login(request).enqueue(new ApiCallback<AuthResponse>(this) {
            @Override
            protected void onSuccess(Response<AuthResponse> response) {
                setLoading(false, "Войти");

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
        });
    }

    private void showResetPasswordDialog() {
        String email = binding.email.getText().toString().trim();

        if (email.isEmpty()) {
            binding.email.setError("Введите email");
            showToast("Сначала введите email");
            return;
        }

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        DialogPasswordResetBinding dialogBinding = DialogPasswordResetBinding.inflate(getLayoutInflater());

        dialog.setContentView(dialogBinding.getRoot());

        dialogBinding.editResetEmail.setText(email);

        dialogBinding.buttonCancelPasswordReset.setOnClickListener(view -> dialog.dismiss());

        setupPasswordToggle(dialogBinding.editResetNewPassword, dialogBinding.btnToggleResetPassword);

        dialogBinding.buttonSendPasswordResetCode.setOnClickListener(view -> {
            String resetEmail = dialogBinding.editResetEmail.getText().toString().trim();

            if (resetEmail.isEmpty()) {
                dialogBinding.editResetEmail.setError("Введите почту");
                return;
            }

            requestPasswordResetCode(dialogBinding, resetEmail);
        });

        dialogBinding.buttonConfirmPasswordReset.setOnClickListener(view -> {
            String resetEmail = dialogBinding.editResetEmail.getText().toString().trim();
            String code = dialogBinding.editResetCode.getText().toString().trim();
            String newPassword = dialogBinding.editResetNewPassword.getText().toString().trim();

            if (resetEmail.isEmpty()) {
                dialogBinding.editResetEmail.setError("Введите почту");
                return;
            }

            if (code.isEmpty()) {
                dialogBinding.editResetCode.setError("Введите код");
                return;
            }

            if (newPassword.isEmpty()) {
                dialogBinding.editResetNewPassword.setError("Введите новый пароль");
                return;
            }

            if (newPassword.length() < 8) {
                dialogBinding.editResetNewPassword.setError("Минимум 8 символов");
                return;
            }

            confirmPasswordReset(dialogBinding, dialog, resetEmail, code, newPassword);
        });

        dialog.show();
    }

    private void requestPasswordResetCode(
            DialogPasswordResetBinding dialogBinding,
            String email
    ) {
        dialogBinding.progressPasswordReset.setVisibility(android.view.View.VISIBLE);
        dialogBinding.buttonSendPasswordResetCode.setEnabled(false);
        dialogBinding.buttonConfirmPasswordReset.setEnabled(false);

        PasswordResetRequest request = new PasswordResetRequest(email);

        authApi.requestPasswordReset(request).enqueue(new ApiCallback<MessageResponse>(this) {
            @Override
            protected void onSuccess(Response<MessageResponse> response) {
                dialogBinding.progressPasswordReset.setVisibility(android.view.View.GONE);
                dialogBinding.buttonSendPasswordResetCode.setEnabled(true);
                dialogBinding.buttonConfirmPasswordReset.setEnabled(true);

                MessageResponse body = response.body();
                String devCode = body != null ? body.getDevCode() : null;

                if (devCode != null) {
                    dialogBinding.editResetCode.setText(devCode);
                    Toast.makeText(
                            SigninActivity.this,
                            "Dev mode: код подставлен автоматически",
                            Toast.LENGTH_SHORT
                    ).show();
                } else {
                    Toast.makeText(
                            SigninActivity.this,
                            "Код отправлен на почту",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        });
    }

    private void confirmPasswordReset(
            DialogPasswordResetBinding dialogBinding,
            BottomSheetDialog dialog,
            String email,
            String code,
            String newPassword
    ) {
        dialogBinding.progressPasswordReset.setVisibility(android.view.View.VISIBLE);
        dialogBinding.buttonSendPasswordResetCode.setEnabled(false);
        dialogBinding.buttonConfirmPasswordReset.setEnabled(false);

        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest(
                email,
                code,
                newPassword
        );

        authApi.confirmPasswordReset(request).enqueue(new ApiCallback<AuthResponse>(this) {
            @Override
            protected void onSuccess(Response<AuthResponse> response) {
                dialogBinding.progressPasswordReset.setVisibility(android.view.View.GONE);
                dialogBinding.buttonSendPasswordResetCode.setEnabled(true);
                dialogBinding.buttonConfirmPasswordReset.setEnabled(true);

                Toast.makeText(
                        SigninActivity.this,
                        "Пароль успешно изменён. Войдите с новым паролем.",
                        Toast.LENGTH_LONG
                ).show();

                dialog.dismiss();
            }
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

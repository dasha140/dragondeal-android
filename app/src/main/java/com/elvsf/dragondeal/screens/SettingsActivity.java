package com.elvsf.dragondeal.screens;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.elvsf.dragondeal.databinding.DialogPasswordResetBinding;
import com.elvsf.dragondeal.dto.PasswordResetRequest;
import com.elvsf.dragondeal.dto.PasswordResetConfirmRequest;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.elvsf.dragondeal.dto.AuthResponse;
import com.elvsf.dragondeal.dto.ConfirmEmailChangeRequest;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.elvsf.dragondeal.R;
import com.elvsf.dragondeal.api.RetrofitClient;
import com.elvsf.dragondeal.api.UploadApi;
import com.elvsf.dragondeal.api.UserApi;
import com.elvsf.dragondeal.databinding.ActivitySettingsBinding;
import com.elvsf.dragondeal.databinding.DialogEditProfileBinding;
import com.elvsf.dragondeal.dto.UpdateUserProfileRequest;
import com.elvsf.dragondeal.dto.UploadImageResponse;
import com.elvsf.dragondeal.dto.UserResponse;
import com.elvsf.dragondeal.storage.TokenStorage;
import com.elvsf.dragondeal.ui.DarkDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;import com.elvsf.dragondeal.ui.BottomNavHelper;import com.elvsf.dragondeal.databinding.DialogPersonalDataBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;import com.elvsf.dragondeal.dto.AuthResponse;
import com.elvsf.dragondeal.dto.UpdatePersonalDataRequest;import com.elvsf.dragondeal.databinding.DialogChangeEmailBinding;import com.elvsf.dragondeal.api.AuthApi;
import com.elvsf.dragondeal.dto.RequestEmailChangeRequest;
import com.elvsf.dragondeal.dto.ConfirmEmailChangeRequest;
import com.elvsf.dragondeal.dto.MessageResponse;
import com.elvsf.dragondeal.util.BackendErrorParser;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private TokenStorage tokenStorage;
    private UserApi userApi;

    private AuthApi authApi;
    private UploadApi uploadApi;

    private ActivityResultLauncher<String> avatarPickerLauncher;
    private DialogEditProfileBinding activeProfileDialogBinding;
    private Uri selectedAvatarUri;

    private String currentDisplayName;
    private String currentUsername;
    private String currentAvatarUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenStorage = new TokenStorage(this);
        userApi = RetrofitClient.getInstance(this).create(UserApi.class);
        authApi = RetrofitClient.getInstance(this).create(AuthApi.class);
        uploadApi = RetrofitClient.getInstance(this).create(UploadApi.class);

        setupAvatarPicker();
        setupProfileData();
        setupClicks();
        BottomNavHelper.setup(this, BottomNavHelper.Tab.SETTINGS);
        loadCurrentUserFromBackend();
    }

    private void setupAvatarPicker() {
        avatarPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null) {
                        return;
                    }

                    selectedAvatarUri = uri;

                    if (activeProfileDialogBinding != null) {
                        activeProfileDialogBinding.dialogImageProfileAvatar.setPadding(0, 0, 0, 0);

                        Glide.with(this)
                                .load(uri)
                                .circleCrop()
                                .placeholder(R.drawable.outline_account_circle_24)
                                .error(R.drawable.outline_account_circle_24)
                                .into(activeProfileDialogBinding.dialogImageProfileAvatar);
                    }
                }
        );
    }

    private void setupProfileData() {
        String username = tokenStorage.getUsername();

        if (username == null || username.trim().isEmpty()) {
            username = "user";
        }

        currentUsername = username;
        currentDisplayName = username;
        currentAvatarUrl = "";

        binding.textProfileName.setText(currentDisplayName);
        binding.textProfileHandle.setText("@" + currentUsername);
        binding.textProfileRating.setText("0.00");
        binding.textProfileCompletedOrders.setText("0 сделок");
    }

    private void loadCurrentUserFromBackend() {
        Long userId = tokenStorage.getUserId();

        if (userId == null) {
            return;
        }

        userApi.findUserById(userId).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<UserResponse> call,
                    @NonNull Response<UserResponse> response
            ) {
                if (!response.isSuccessful() || response.body() == null) {
                    return;
                }

                UserResponse user = response.body();

                currentUsername = user.getUsername();
                currentDisplayName = user.getNameForDisplay();
                currentAvatarUrl = user.getAvatarUrl();

                binding.textProfileName.setText(currentDisplayName);

                if (currentUsername != null && !currentUsername.trim().isEmpty()) {
                    binding.textProfileHandle.setText("@" + currentUsername);
                }

                if (user.getRating() != null) {
                    binding.textProfileRating.setText(user.getRating().toString());
                } else {
                    binding.textProfileRating.setText("0.00");
                }

                Long completedOrdersCount = user.getCompletedOrdersCount();

                if (completedOrdersCount == null) {
                    completedOrdersCount = 0L;
                }

                binding.textProfileCompletedOrders.setText(completedOrdersCount + " сделок");

                loadAvatarIntoMainProfile();
            }

            @Override
            public void onFailure(
                    @NonNull Call<UserResponse> call,
                    @NonNull Throwable t
            ) {
            }
        });
    }

    private void setupClicks() {
        binding.buttonSettingsLogout.setOnClickListener(view -> logout());

        binding.buttonEditProfile.setOnClickListener(view -> showEditProfileDialog());

        binding.itemPersonalData.setOnClickListener(view -> showPersonalDataDialog());

        binding.itemMyListings.setOnClickListener(view -> {
            Intent intent = new Intent(SettingsActivity.this, MyListingsActivity.class);
            startActivity(intent);
        });

        binding.itemDealsHistory.setOnClickListener(view -> openOrders());

        binding.layoutSecurity.setOnClickListener(view -> showPasswordResetDialog());


        binding.itemSupport.setOnClickListener(view -> {
            Intent intent = new Intent(SettingsActivity.this, SupportActivity.class);
            startActivity(intent);
        });

        binding.itemAbout.setOnClickListener(view -> {
            Intent intent = new Intent(SettingsActivity.this, AboutActivity.class);
            startActivity(intent);
        });
    }

    private void showEditProfileDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        DialogEditProfileBinding dialogBinding = DialogEditProfileBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());

        activeProfileDialogBinding = dialogBinding;
        selectedAvatarUri = null;

        dialogBinding.editDisplayName.setText(currentDisplayName);

        if (currentAvatarUrl != null && !currentAvatarUrl.trim().isEmpty()) {
            dialogBinding.dialogImageProfileAvatar.setPadding(0, 0, 0, 0);

            Glide.with(this)
                    .load(buildFileUrl(currentAvatarUrl))
                    .circleCrop()
                    .placeholder(R.drawable.outline_account_circle_24)
                    .error(R.drawable.outline_account_circle_24)
                    .into(dialogBinding.dialogImageProfileAvatar);
        }

        dialogBinding.buttonChooseProfilePhoto.setOnClickListener(view ->
                avatarPickerLauncher.launch("image/*")
        );

        dialogBinding.buttonCancelProfile.setOnClickListener(view -> dialog.dismiss());

        dialogBinding.buttonSaveProfile.setOnClickListener(view -> {
            String newDisplayName = dialogBinding.editDisplayName.getText().toString().trim();

            if (newDisplayName.isEmpty()) {
                dialogBinding.editDisplayName.setError("Введите имя");
                return;
            }

            if (newDisplayName.length() > 80) {
                dialogBinding.editDisplayName.setError("Максимум 80 символов");
                return;
            }

            saveProfile(dialog, dialogBinding, newDisplayName);
        });

        dialog.setOnDismissListener(dialogInterface -> {
            activeProfileDialogBinding = null;
            selectedAvatarUri = null;
        });

        dialog.show();

        Window window = dialog.getWindow();

        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.90),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void saveProfile(
            Dialog dialog,
            DialogEditProfileBinding dialogBinding,
            String displayName
    ) {
        setDialogLoading(dialogBinding, true);

        if (selectedAvatarUri != null) {
            uploadAvatarThenUpdateProfile(dialog, dialogBinding, displayName);
            return;
        }

        updateProfileOnBackend(dialog, dialogBinding, displayName, currentAvatarUrl);
    }

    private void uploadAvatarThenUpdateProfile(
            Dialog dialog,
            DialogEditProfileBinding dialogBinding,
            String displayName
    ) {
        try {
            File file = createTempFileFromUri(selectedAvatarUri);

            RequestBody requestBody = RequestBody.create(
                    file,
                    MediaType.parse("image/*")
            );

            MultipartBody.Part part = MultipartBody.Part.createFormData(
                    "file",
                    file.getName(),
                    requestBody
            );

            uploadApi.uploadAvatar(part).enqueue(new Callback<UploadImageResponse>() {
                @Override
                public void onResponse(
                        @NonNull Call<UploadImageResponse> call,
                        @NonNull Response<UploadImageResponse> response
                ) {
                    if (!response.isSuccessful() || response.body() == null) {
                        setDialogLoading(dialogBinding, false);
                        String errorMessage = BackendErrorParser.parse(response);
                        if (handleAuthError(errorMessage, dialog)) return;
                        DarkDialog.showMessage(
                                SettingsActivity.this,
                                "Ошибка",
                                errorMessage
                        );
                        return;
                    }

                    String avatarUrl = response.body().getImageUrl();
                    updateProfileOnBackend(dialog, dialogBinding, displayName, avatarUrl);
                }

                @Override
                public void onFailure(
                        @NonNull Call<UploadImageResponse> call,
                        @NonNull Throwable t
                ) {
                    setDialogLoading(dialogBinding, false);

                    Toast.makeText(
                            SettingsActivity.this,
                            BackendErrorParser.parse(t),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });
        } catch (Exception exception) {
            setDialogLoading(dialogBinding, false);

            Toast.makeText(
                    SettingsActivity.this,
                    "Не удалось подготовить файл: " + exception.getMessage(),
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void updateProfileOnBackend(
            Dialog dialog,
            DialogEditProfileBinding dialogBinding,
            String displayName,
            String avatarUrl
    ) {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest(
                displayName,
                avatarUrl
        );

        userApi.updateMyProfile(request).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<UserResponse> call,
                    @NonNull Response<UserResponse> response
            ) {
                setDialogLoading(dialogBinding, false);

                if (!response.isSuccessful() || response.body() == null) {
                    String errorMessage = BackendErrorParser.parse(response);
                    if (handleAuthError(errorMessage, dialog)) return;
                    DarkDialog.showMessage(
                            SettingsActivity.this,
                            "Ошибка",
                            errorMessage
                    );
                    return;
                }

                UserResponse user = response.body();

                currentDisplayName = user.getNameForDisplay();
                currentUsername = user.getUsername();
                currentAvatarUrl = user.getAvatarUrl();

                binding.textProfileName.setText(currentDisplayName);

                if (currentUsername != null && !currentUsername.trim().isEmpty()) {
                    binding.textProfileHandle.setText("@" + currentUsername);
                }

                loadAvatarIntoMainProfile();

                Toast.makeText(SettingsActivity.this, "Профиль обновлён", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }

            @Override
            public void onFailure(
                    @NonNull Call<UserResponse> call,
                    @NonNull Throwable t
            ) {
                setDialogLoading(dialogBinding, false);

                Toast.makeText(
                        SettingsActivity.this,
                        BackendErrorParser.parse(t),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void setDialogLoading(
            DialogEditProfileBinding dialogBinding,
            boolean loading
    ) {
        dialogBinding.progressDialogProfile.setVisibility(loading ? View.VISIBLE : View.GONE);
        dialogBinding.buttonSaveProfile.setEnabled(!loading);
        dialogBinding.buttonCancelProfile.setEnabled(!loading);
        dialogBinding.buttonChooseProfilePhoto.setEnabled(!loading);
    }

    private void loadAvatarIntoMainProfile() {
        if (currentAvatarUrl == null || currentAvatarUrl.trim().isEmpty()) {
            binding.imageProfileAvatar.setPadding(dp(18), dp(18), dp(18), dp(18));
            binding.imageProfileAvatar.setColorFilter(Color.parseColor("#FF3B30"));
            binding.imageProfileAvatar.setImageResource(R.drawable.outline_account_circle_24);
            return;
        }

        binding.imageProfileAvatar.setPadding(0, 0, 0, 0);
        binding.imageProfileAvatar.clearColorFilter();

        Glide.with(this)
                .load(buildFileUrl(currentAvatarUrl))
                .circleCrop()
                .placeholder(R.drawable.outline_account_circle_24)
                .error(R.drawable.outline_account_circle_24)
                .into(binding.imageProfileAvatar);
    }

    private String buildFileUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            return "";
        }

        if (fileUrl.startsWith("http://") || fileUrl.startsWith("https://")) {
            return fileUrl;
        }

        String baseUrl = RetrofitClient.getInstance(this).baseUrl().toString();

        if (baseUrl.endsWith("/api/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 5);
        } else if (baseUrl.endsWith("/api")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 4);
        }

        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        if (!fileUrl.startsWith("/")) {
            fileUrl = "/" + fileUrl;
        }

        return baseUrl + fileUrl;
    }

    private File createTempFileFromUri(Uri uri) throws Exception {
        String fileName = getFileNameFromUri(uri);

        if (fileName == null || fileName.trim().isEmpty()) {
            fileName = "avatar.jpg";
        }

        File file = new File(getCacheDir(), fileName);

        try (
                InputStream inputStream = getContentResolver().openInputStream(uri);
                FileOutputStream outputStream = new FileOutputStream(file)
        ) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Не удалось открыть файл");
            }

            byte[] buffer = new byte[8192];
            int length;

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }

        return file;
    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;

        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);

                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }

        if (result == null) {
            result = uri.getLastPathSegment();
        }

        return result;
    }

    private void openOrders() {
        Intent intent = new Intent(SettingsActivity.this, OrdersActivity.class);
        startActivity(intent);
    }

    /**
     * Detect auth errors in API responses and handle session expiration.
     * Returns true if the error was an auth error (caller should return early).
     */
    private boolean handleAuthError(String errorMessage, android.app.Dialog dialog) {
        if (errorMessage != null && errorMessage.contains("Сессия истекла")) {
            if (dialog != null) {
                dialog.dismiss();
            }

            tokenStorage.clear();

            DarkDialog.showConfirm(
                    this,
                    "Сессия истекла",
                    "Войдите в аккаунт снова.",
                    "Войти",
                    "Закрыть",
                    () -> {
                        Intent intent = new Intent(SettingsActivity.this, SigninActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
            );

            return true;
        }

        return false;
    }

    private void logout() {
        tokenStorage.clear();

        Intent intent = new Intent(SettingsActivity.this, SigninActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showPersonalDataDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);

        DialogPersonalDataBinding dialogBinding =
                DialogPersonalDataBinding.inflate(getLayoutInflater());

        dialog.setContentView(dialogBinding.getRoot());

        String currentName = binding.textProfileName.getText().toString().trim();

        if (!currentName.isEmpty()) {
            dialogBinding.editPersonalName.setText(currentName);
        }

        String currentEmail = tokenStorage.getEmail();

        if (currentEmail != null && !currentEmail.trim().isEmpty()) {
            dialogBinding.editPersonalEmail.setText(currentEmail);
        }

        dialogBinding.buttonChangeEmail.setOnClickListener(view -> {
            dialog.dismiss();
            showChangeEmailDialog();
        });

        dialogBinding.buttonSavePersonalData.setOnClickListener(view -> {
            String name = dialogBinding.editPersonalName.getText().toString().trim();

            if (name.isEmpty()) {
                dialogBinding.editPersonalName.setError("Введите никнейм");
                return;
            }

            String email = tokenStorage.getEmail();

            if (email == null || email.trim().isEmpty()) {
                email = dialogBinding.editPersonalEmail.getText().toString().trim();
            }

            updatePersonalData(dialog, dialogBinding, name, email);
        });

        dialog.show();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private void updatePersonalData(
            BottomSheetDialog dialog,
            DialogPersonalDataBinding dialogBinding,
            String displayName,
            String email
    ) {
        dialogBinding.progressPersonalData.setVisibility(android.view.View.VISIBLE);
        dialogBinding.buttonSavePersonalData.setEnabled(false);
        dialogBinding.buttonCancelPersonalData.setEnabled(false);

        UpdatePersonalDataRequest request = new UpdatePersonalDataRequest(
                displayName,
                email
        );

        userApi.updatePersonalData(request).enqueue(new retrofit2.Callback<AuthResponse>() {
            @Override
            public void onResponse(
                    @NonNull retrofit2.Call<AuthResponse> call,
                    @NonNull retrofit2.Response<AuthResponse> response
            ) {
                dialogBinding.progressPersonalData.setVisibility(android.view.View.GONE);
                dialogBinding.buttonSavePersonalData.setEnabled(true);
                dialogBinding.buttonCancelPersonalData.setEnabled(true);

                if (!response.isSuccessful() || response.body() == null) {
                    String errorMessage = BackendErrorParser.parse(response);
                    if (handleAuthError(errorMessage, null)) return;
                    Toast.makeText(SettingsActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    return;
                }

                AuthResponse authResponse = response.body();

                currentDisplayName = authResponse.getNameForDisplay();
                currentUsername = authResponse.getUsername();

                binding.textProfileName.setText(currentDisplayName);

                if (currentUsername != null && !currentUsername.trim().isEmpty()) {
                    binding.textProfileHandle.setText("@" + currentUsername);
                }

                if (authResponse.getAccessToken() != null && !authResponse.getAccessToken().trim().isEmpty()) {
                    tokenStorage.saveAuthData(
                            authResponse.getAccessToken(),
                            authResponse.getId(),
                            authResponse.getUsername(),
                            authResponse.getEmail(),
                            authResponse.getRole()
                    );
                }

                Toast.makeText(SettingsActivity.this, "Личные данные обновлены", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                loadCurrentUserFromBackend();
            }

            @Override
            public void onFailure(
                    @NonNull retrofit2.Call<AuthResponse> call,
                    @NonNull Throwable t
            ) {
                dialogBinding.progressPersonalData.setVisibility(android.view.View.GONE);
                dialogBinding.buttonSavePersonalData.setEnabled(true);
                dialogBinding.buttonCancelPersonalData.setEnabled(true);

                Toast.makeText(
                        SettingsActivity.this,
                        BackendErrorParser.parse(t),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void showChangeEmailDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);

        DialogChangeEmailBinding dialogBinding =
                DialogChangeEmailBinding.inflate(getLayoutInflater());

        dialog.setContentView(dialogBinding.getRoot());

        dialogBinding.buttonCancelEmailChange.setOnClickListener(view -> dialog.dismiss());

        dialogBinding.buttonSendEmailCode.setOnClickListener(view -> {
            String newEmail = dialogBinding.editNewEmail.getText().toString().trim();
            String currentPassword = dialogBinding.editCurrentPassword.getText().toString().trim();

            if (newEmail.isEmpty()) {
                dialogBinding.editNewEmail.setError("Введите новую почту");
                return;
            }

            if (currentPassword.isEmpty()) {
                dialogBinding.editCurrentPassword.setError("Введите текущий пароль");
                return;
            }

            checkEmailAndRequestChangeCode(dialogBinding, newEmail, currentPassword);
        });

        dialogBinding.buttonConfirmEmailChange.setOnClickListener(view -> {
            String newEmail = dialogBinding.editNewEmail.getText().toString().trim();
            String code = dialogBinding.editEmailCode.getText().toString().trim();

            if (newEmail.isEmpty()) {
                dialogBinding.editNewEmail.setError("Введите новую почту");
                return;
            }

            if (code.isEmpty()) {
                dialogBinding.editEmailCode.setError("Введите код");
                return;
            }

            confirmEmailChange(dialogBinding, newEmail, code, dialog);
        });

        dialog.show();
    }

    private void requestEmailChangeCode(
            DialogChangeEmailBinding dialogBinding,
            String newEmail,
            String currentPassword
    ) {
        dialogBinding.progressChangeEmail.setVisibility(View.VISIBLE);
        dialogBinding.buttonSendEmailCode.setEnabled(false);
        dialogBinding.buttonConfirmEmailChange.setEnabled(false);

        RequestEmailChangeRequest request = new RequestEmailChangeRequest(
                newEmail,
                currentPassword
        );

        authApi.requestEmailChange(request).enqueue(new retrofit2.Callback<MessageResponse>() {
            @Override
            public void onResponse(
                    @NonNull retrofit2.Call<MessageResponse> call,
                    @NonNull retrofit2.Response<MessageResponse> response
            ) {
                dialogBinding.progressChangeEmail.setVisibility(View.GONE);
                dialogBinding.buttonSendEmailCode.setEnabled(true);
                dialogBinding.buttonConfirmEmailChange.setEnabled(true);

                if (!response.isSuccessful()) {
                    String errorMessage = BackendErrorParser.parse(response);
                    DarkDialog.showMessage(
                            SettingsActivity.this,
                            "Ошибка смены почты",
                            errorMessage
                    );
                    return;
                }

                MessageResponse body = response.body();
                String devCode = body != null ? body.getDevCode() : null;

                if (devCode != null) {
                    dialogBinding.editEmailCode.setText(devCode);
                    Toast.makeText(
                            SettingsActivity.this,
                            "Dev mode: код подставлен автоматически",
                            Toast.LENGTH_SHORT
                    ).show();
                } else {
                    Toast.makeText(
                            SettingsActivity.this,
                            "Код отправлен на новую почту",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    @NonNull retrofit2.Call<MessageResponse> call,
                    @NonNull Throwable t
            ) {
                dialogBinding.progressChangeEmail.setVisibility(View.GONE);
                dialogBinding.buttonSendEmailCode.setEnabled(true);
                dialogBinding.buttonConfirmEmailChange.setEnabled(true);

                Toast.makeText(
                        SettingsActivity.this,
                        BackendErrorParser.parse(t),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void checkEmailAndRequestChangeCode(
            DialogChangeEmailBinding dialogBinding,
            String newEmail,
            String currentPassword
    ) {
        String currentEmail = tokenStorage.getEmail();

        if (currentEmail != null && newEmail.equalsIgnoreCase(currentEmail.trim())) {
            dialogBinding.editNewEmail.setError("Это уже ваша текущая почта");
            return;
        }

        dialogBinding.progressChangeEmail.setVisibility(View.VISIBLE);
        dialogBinding.buttonSendEmailCode.setEnabled(false);
        dialogBinding.buttonConfirmEmailChange.setEnabled(false);

        authApi.checkEmail(newEmail).enqueue(new retrofit2.Callback<MessageResponse>() {
            @Override
            public void onResponse(
                    @NonNull retrofit2.Call<MessageResponse> call,
                    @NonNull retrofit2.Response<MessageResponse> response
            ) {
                dialogBinding.progressChangeEmail.setVisibility(View.GONE);
                dialogBinding.buttonSendEmailCode.setEnabled(true);
                dialogBinding.buttonConfirmEmailChange.setEnabled(true);

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(
                            SettingsActivity.this,
                            BackendErrorParser.parse(response),
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                String message = response.body().getMessage();

                if ("TAKEN".equalsIgnoreCase(message)) {
                    dialogBinding.editNewEmail.setError("Эта почта уже занята");
                    Toast.makeText(
                            SettingsActivity.this,
                            "Эта почта уже занята",
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }

                requestEmailChangeCode(dialogBinding, newEmail, currentPassword);
            }

            @Override
            public void onFailure(
                    @NonNull retrofit2.Call<MessageResponse> call,
                    @NonNull Throwable t
            ) {
                dialogBinding.progressChangeEmail.setVisibility(View.GONE);
                dialogBinding.buttonSendEmailCode.setEnabled(true);
                dialogBinding.buttonConfirmEmailChange.setEnabled(true);

                Toast.makeText(
                        SettingsActivity.this,
                        BackendErrorParser.parse(t),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void confirmEmailChange(
            DialogChangeEmailBinding dialogBinding,
            String newEmail,
            String code,
            com.google.android.material.bottomsheet.BottomSheetDialog dialog
    ) {
        dialogBinding.progressChangeEmail.setVisibility(View.VISIBLE);
        dialogBinding.buttonSendEmailCode.setEnabled(false);
        dialogBinding.buttonConfirmEmailChange.setEnabled(false);

        ConfirmEmailChangeRequest request = new ConfirmEmailChangeRequest(newEmail, code);

        authApi.confirmEmailChange(request).enqueue(new retrofit2.Callback<AuthResponse>() {
            @Override
            public void onResponse(
                    @NonNull retrofit2.Call<AuthResponse> call,
                    @NonNull retrofit2.Response<AuthResponse> response
            ) {
                dialogBinding.progressChangeEmail.setVisibility(View.GONE);
                dialogBinding.buttonSendEmailCode.setEnabled(true);
                dialogBinding.buttonConfirmEmailChange.setEnabled(true);

                if (!response.isSuccessful() || response.body() == null) {
                    String errorMessage = BackendErrorParser.parse(response);
                    DarkDialog.showMessage(
                            SettingsActivity.this,
                            "Ошибка смены почты",
                            errorMessage
                    );

                    return;
                }

                AuthResponse authResponse = response.body();

                tokenStorage.saveAuthData(
                        authResponse.getAccessToken(),
                        authResponse.getId(),
                        authResponse.getUsername(),
                        authResponse.getEmail(),
                        "USER"
                );

                Toast.makeText(
                        SettingsActivity.this,
                        "Почта успешно изменена",
                        Toast.LENGTH_SHORT
                ).show();

                dialog.dismiss();
                recreate();
            }

            @Override
            public void onFailure(
                    @NonNull retrofit2.Call<AuthResponse> call,
                    @NonNull Throwable t
            ) {
                dialogBinding.progressChangeEmail.setVisibility(View.GONE);
                dialogBinding.buttonSendEmailCode.setEnabled(true);
                dialogBinding.buttonConfirmEmailChange.setEnabled(true);

                Toast.makeText(
                        SettingsActivity.this,
                        BackendErrorParser.parse(t),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void showPasswordResetDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        DialogPasswordResetBinding dialogBinding = DialogPasswordResetBinding.inflate(getLayoutInflater());

        dialog.setContentView(dialogBinding.getRoot());

        String currentEmail = tokenStorage.getEmail();
        if (currentEmail != null && !currentEmail.isBlank()) {
            dialogBinding.editResetEmail.setText(currentEmail);
        }

        dialogBinding.buttonCancelPasswordReset.setOnClickListener(view -> dialog.dismiss());

        dialogBinding.buttonSendPasswordResetCode.setOnClickListener(view -> {
            String email = dialogBinding.editResetEmail.getText().toString().trim();

            if (email.isEmpty()) {
                dialogBinding.editResetEmail.setError("Введите почту");
                return;
            }

            requestPasswordResetCode(dialogBinding, email);
        });

        dialogBinding.buttonConfirmPasswordReset.setOnClickListener(view -> {
            String email = dialogBinding.editResetEmail.getText().toString().trim();
            String code = dialogBinding.editResetCode.getText().toString().trim();
            String newPassword = dialogBinding.editResetNewPassword.getText().toString().trim();

            if (email.isEmpty()) {
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

            confirmPasswordReset(dialogBinding, dialog, email, code, newPassword);
        });

        dialog.show();
    }

    private void requestPasswordResetCode(
            DialogPasswordResetBinding dialogBinding,
            String email
    ) {
        dialogBinding.progressPasswordReset.setVisibility(View.VISIBLE);
        dialogBinding.buttonSendPasswordResetCode.setEnabled(false);
        dialogBinding.buttonConfirmPasswordReset.setEnabled(false);

        PasswordResetRequest request = new PasswordResetRequest(email);

        authApi.requestPasswordReset(request).enqueue(new retrofit2.Callback<MessageResponse>() {
            @Override
            public void onResponse(
                    @NonNull retrofit2.Call<MessageResponse> call,
                    @NonNull retrofit2.Response<MessageResponse> response
            ) {
                dialogBinding.progressPasswordReset.setVisibility(View.GONE);
                dialogBinding.buttonSendPasswordResetCode.setEnabled(true);
                dialogBinding.buttonConfirmPasswordReset.setEnabled(true);

                if (!response.isSuccessful()) {
                    Toast.makeText(
                            SettingsActivity.this,
                            BackendErrorParser.parse(response),
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                MessageResponse body = response.body();
                String devCode = body != null ? body.getDevCode() : null;

                if (devCode != null) {
                    dialogBinding.editResetCode.setText(devCode);
                    Toast.makeText(
                            SettingsActivity.this,
                            "Dev mode: код подставлен автоматически",
                            Toast.LENGTH_SHORT
                    ).show();
                } else {
                    Toast.makeText(
                            SettingsActivity.this,
                            "Код отправлен на почту",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    @NonNull retrofit2.Call<MessageResponse> call,
                    @NonNull Throwable t
            ) {
                dialogBinding.progressPasswordReset.setVisibility(View.GONE);
                dialogBinding.buttonSendPasswordResetCode.setEnabled(true);
                dialogBinding.buttonConfirmPasswordReset.setEnabled(true);

                Toast.makeText(
                        SettingsActivity.this,
                        BackendErrorParser.parse(t),
                        Toast.LENGTH_LONG
                ).show();
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
        dialogBinding.progressPasswordReset.setVisibility(View.VISIBLE);
        dialogBinding.buttonSendPasswordResetCode.setEnabled(false);
        dialogBinding.buttonConfirmPasswordReset.setEnabled(false);

        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest(
                email,
                code,
                newPassword
        );

        authApi.confirmPasswordReset(request).enqueue(new retrofit2.Callback<AuthResponse>() {
            @Override
            public void onResponse(
                    @NonNull retrofit2.Call<AuthResponse> call,
                    @NonNull retrofit2.Response<AuthResponse> response
            ) {
                dialogBinding.progressPasswordReset.setVisibility(View.GONE);
                dialogBinding.buttonSendPasswordResetCode.setEnabled(true);
                dialogBinding.buttonConfirmPasswordReset.setEnabled(true);

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(
                            SettingsActivity.this,
                            BackendErrorParser.parse(response),
                            Toast.LENGTH_LONG
                    ).show();
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

                Toast.makeText(
                        SettingsActivity.this,
                        "Пароль успешно изменён",
                        Toast.LENGTH_SHORT
                ).show();

                dialog.dismiss();
            }

            @Override
            public void onFailure(
                    @NonNull retrofit2.Call<AuthResponse> call,
                    @NonNull Throwable t
            ) {
                dialogBinding.progressPasswordReset.setVisibility(View.GONE);
                dialogBinding.buttonSendPasswordResetCode.setEnabled(true);
                dialogBinding.buttonConfirmPasswordReset.setEnabled(true);

                Toast.makeText(
                        SettingsActivity.this,
                        BackendErrorParser.parse(t),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}
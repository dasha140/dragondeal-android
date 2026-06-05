package com.elvsf.dragondeal.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.ViewGroup;
import android.view.Window;

import com.elvsf.dragondeal.databinding.DialogAuthRequiredBinding;
import com.elvsf.dragondeal.screens.SigninActivity;
import com.elvsf.dragondeal.screens.SignupActivity;
import com.elvsf.dragondeal.storage.TokenStorage;

public final class AuthGate {

    private AuthGate() {
    }

    public static boolean requireAuth(Activity activity) {
        return requireAuth(activity, "Для этого действия нужно войти в аккаунт.");
    }

    public static boolean requireAuth(Activity activity, String message) {
        TokenStorage tokenStorage = new TokenStorage(activity);

        if (tokenStorage.isLoggedIn()) {
            return true;
        }

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        DialogAuthRequiredBinding binding = DialogAuthRequiredBinding.inflate(activity.getLayoutInflater());
        dialog.setContentView(binding.getRoot());

        binding.textAuthRequiredMessage.setText(message);

        binding.buttonAuthLogin.setOnClickListener(v -> {
            dialog.dismiss();
            activity.startActivity(new Intent(activity, SigninActivity.class));
        });

        binding.buttonAuthSignup.setOnClickListener(v -> {
            dialog.dismiss();
            activity.startActivity(new Intent(activity, SignupActivity.class));
        });

        binding.buttonAuthCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(
                    (int) (activity.getResources().getDisplayMetrics().widthPixels * 0.90),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        return false;
    }
}

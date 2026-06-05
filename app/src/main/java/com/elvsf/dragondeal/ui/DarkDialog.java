package com.elvsf.dragondeal.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.Window;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DarkDialog {

    public static void showMessage(
            Context context,
            String title,
            String message
    ) {
        Dialog dialog = createBaseDialog(context);

        LinearLayout root = createRoot(context);

        TextView titleView = createTitle(context, title);
        TextView messageView = createMessage(context, message);

        Button okButton = createButton(context, "OK", "#FF3B30", "#FFFFFF");
        okButton.setOnClickListener(view -> dialog.dismiss());

        root.addView(titleView);
        root.addView(messageView);
        root.addView(okButton);

        show(dialog, root);
    }

    public static void showConfirm(
            Context context,
            String title,
            String message,
            String positiveText,
            String negativeText,
            Runnable onPositive
    ) {
        Dialog dialog = createBaseDialog(context);

        LinearLayout root = createRoot(context);

        TextView titleView = createTitle(context, title);
        TextView messageView = createMessage(context, message);

        Button positiveButton = createButton(context, positiveText, "#FF3B30", "#FFFFFF");
        Button negativeButton = createButton(context, negativeText, "#2A2A2A", "#FFFFFF");

        positiveButton.setOnClickListener(view -> {
            dialog.dismiss();

            if (onPositive != null) {
                onPositive.run();
            }
        });

        negativeButton.setOnClickListener(view -> dialog.dismiss());

        root.addView(titleView);
        root.addView(messageView);
        root.addView(positiveButton);
        root.addView(negativeButton);

        show(dialog, root);
    }

    private static Dialog createBaseDialog(Context context) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    private static LinearLayout createRoot(Context context) {
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(context, 24), dp(context, 24), dp(context, 24), dp(context, 20));
        root.setBackground(createRoundedBackground("#121212", context, 24));
        return root;
    }

    private static TextView createTitle(Context context, String title) {
        TextView textView = new TextView(context);
        textView.setText(title);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(22);
        textView.setTypeface(null, Typeface.BOLD);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        textView.setLayoutParams(params);
        return textView;
    }

    private static TextView createMessage(Context context, String message) {
        TextView textView = new TextView(context);
        textView.setText(message);
        textView.setTextColor(Color.parseColor("#B0B0B0"));
        textView.setTextSize(15);
        textView.setLineSpacing(dp(context, 3), 1.0f);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dp(context, 14), 0, dp(context, 16));

        textView.setLayoutParams(params);
        return textView;
    }

    private static Button createButton(
            Context context,
            String text,
            String backgroundColor,
            String textColor
    ) {
        Button button = new Button(context);
        button.setText(text);
        button.setTextColor(Color.parseColor(textColor));
        button.setTextSize(15);
        button.setAllCaps(false);
        button.setTypeface(null, Typeface.BOLD);
        button.setGravity(Gravity.CENTER);
        button.setBackground(createRoundedBackground(backgroundColor, context, 16));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(context, 52)
        );
        params.setMargins(0, dp(context, 8), 0, 0);

        button.setLayoutParams(params);
        return button;
    }

    private static GradientDrawable createRoundedBackground(
            String color,
            Context context,
            int radiusDp
    ) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.parseColor(color));
        drawable.setCornerRadius(dp(context, radiusDp));
        return drawable;
    }

    private static void show(Dialog dialog, LinearLayout root) {
        dialog.setContentView(root);
        dialog.show();

        Window window = dialog.getWindow();

        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(
                    (int) (root.getResources().getDisplayMetrics().widthPixels * 0.88),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private static int dp(Context context, int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density);
    }
}
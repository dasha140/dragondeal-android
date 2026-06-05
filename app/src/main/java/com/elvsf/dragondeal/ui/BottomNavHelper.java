package com.elvsf.dragondeal.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.elvsf.dragondeal.R;
import com.elvsf.dragondeal.screens.MainActivity;
import com.elvsf.dragondeal.screens.OrdersActivity;
import com.elvsf.dragondeal.screens.SettingsActivity;

public class BottomNavHelper {

    public enum Tab {
        ORDERS,
        HOME,
        SETTINGS
    }

    public static void setup(Activity activity, Tab activeTab) {
        LinearLayout navOrders = activity.findViewById(R.id.navOrders);
        LinearLayout navHome = activity.findViewById(R.id.navHome);
        LinearLayout navSettings = activity.findViewById(R.id.navSettings);

        ImageView iconOrders = activity.findViewById(R.id.iconNavOrders);
        ImageView iconHome = activity.findViewById(R.id.iconNavHome);
        ImageView iconSettings = activity.findViewById(R.id.iconNavSettings);

        TextView textOrders = activity.findViewById(R.id.textNavOrders);
        TextView textHome = activity.findViewById(R.id.textNavHome);
        TextView textSettings = activity.findViewById(R.id.textNavSettings);

        setInactive(iconOrders, textOrders);
        setInactive(iconHome, textHome);
        setInactive(iconSettings, textSettings);

        if (activeTab == Tab.ORDERS) {
            setActive(iconOrders, textOrders);
        } else if (activeTab == Tab.HOME) {
            setActive(iconHome, textHome);
        } else if (activeTab == Tab.SETTINGS) {
            setActive(iconSettings, textSettings);
        }

        navOrders.setOnClickListener(view -> {
            if (activeTab == Tab.ORDERS) {
                return;
            }

            if (!com.elvsf.dragondeal.ui.AuthGate.requireAuth(
                    activity,
                    "Заказы доступны только после входа в аккаунт."
            )) {
                return;
            }

            Intent intent = new Intent(activity, OrdersActivity.class);
            intent.putExtra("ordersMode", "active");
            activity.startActivity(intent);
        });

        navHome.setOnClickListener(view -> {
            if (activeTab == Tab.HOME) {
                return;
            }

            Intent intent = new Intent(activity, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            activity.startActivity(intent);
        });

        navSettings.setOnClickListener(view -> {
            if (!com.elvsf.dragondeal.ui.AuthGate.requireAuth(
                    activity,
                    "Настройки доступны только после входа в аккаунт."
            )) {
                return;
            }

            if (!(activity instanceof com.elvsf.dragondeal.screens.SettingsActivity)) {
                activity.startActivity(
                        new Intent(activity, com.elvsf.dragondeal.screens.SettingsActivity.class)
                );
            }
        });
    }

    private static void setActive(ImageView icon, TextView text) {
        icon.setColorFilter(Color.parseColor("#FF3B30"));
        text.setTextColor(Color.parseColor("#FF3B30"));
        text.setTypeface(null, android.graphics.Typeface.BOLD);
    }

    private static void setInactive(ImageView icon, TextView text) {
        icon.setColorFilter(Color.parseColor("#8E8E93"));
        text.setTextColor(Color.parseColor("#8E8E93"));
        text.setTypeface(null, android.graphics.Typeface.NORMAL);
    }
}
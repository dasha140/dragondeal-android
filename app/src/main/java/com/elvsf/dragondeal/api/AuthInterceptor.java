package com.elvsf.dragondeal.api;

import android.content.Context;

import androidx.annotation.NonNull;

import com.elvsf.dragondeal.BuildConfig;
import com.elvsf.dragondeal.storage.TokenStorage;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final TokenStorage tokenStorage;
    private final boolean useXAuthToken;

    public AuthInterceptor(Context context) {
        this.tokenStorage = new TokenStorage(context.getApplicationContext());
        // Yandex Cloud Functions proxy blocks "Authorization: Bearer <token>" at the gateway level.
        // Use X-Auth-Token header instead when talking to Yandex Cloud backend.
        this.useXAuthToken = isYandexCloudUrl();
    }

    private boolean isYandexCloudUrl() {
        String baseUrl = BuildConfig.BASE_URL;
        return baseUrl != null && baseUrl.contains("containers.yandexcloud.net");
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request originalRequest = chain.request();

        String token = tokenStorage.getAccessToken();

        if (token == null || token.trim().isEmpty()) {
            return chain.proceed(originalRequest);
        }

        String cleanToken = token.trim();

        Request.Builder builder = originalRequest.newBuilder();

        if (useXAuthToken) {
            // Yandex Cloud Functions proxy passes X-Auth-Token through to the backend
            builder.header("X-Auth-Token", cleanToken);
        } else {
            // Direct backend connection — use standard Authorization header
            builder.header("Authorization", "Bearer " + cleanToken);
        }

        return chain.proceed(builder.build());
    }
}
package com.elvsf.dragondeal.api;

import android.util.Log;

import androidx.annotation.NonNull;

import com.elvsf.dragondeal.BuildConfig;
import com.elvsf.dragondeal.storage.TokenStorage;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private static final String TAG = "AuthInterceptor";

    private final TokenStorage tokenStorage;
    private final boolean useXAuthToken;

    public AuthInterceptor(TokenStorage tokenStorage) {
        this.tokenStorage = tokenStorage;
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
        String method = originalRequest.method();
        String url = originalRequest.url().toString();

        String token = tokenStorage.getAccessToken();
        boolean tokenExists = token != null && !token.trim().isEmpty();

        if (tokenExists) {
            String headerName = useXAuthToken ? "X-Auth-Token" : "Authorization";
            Log.d(TAG, "Request: " + method + " " + url
                    + " | tokenExists=true | header=" + headerName
                    + " | yandex=" + useXAuthToken);
        } else {
            Log.d(TAG, "Request: " + method + " " + url
                    + " | tokenExists=false | no auth header added");
        }

        if (!tokenExists) {
            return chain.proceed(originalRequest);
        }

        String cleanToken = token.trim();

        Request.Builder builder = originalRequest.newBuilder();

        if (useXAuthToken) {
            builder.header("X-Auth-Token", cleanToken);
        } else {
            builder.header("Authorization", "Bearer " + cleanToken);
        }

        Response response = chain.proceed(builder.build());

        if (response.code() == 400 || response.code() == 403) {
            Log.e(TAG, "Auth error: " + response.code() + " "
                    + method + " " + url
                    + " | tokenExists=" + tokenExists
                    + " | header=" + (useXAuthToken ? "X-Auth-Token" : "Authorization"));
        }

        return response;
    }
}
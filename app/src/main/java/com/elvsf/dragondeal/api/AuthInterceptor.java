package com.elvsf.dragondeal.api;

import android.content.Context;

import androidx.annotation.NonNull;

import com.elvsf.dragondeal.storage.TokenStorage;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final TokenStorage tokenStorage;

    public AuthInterceptor(Context context) {
        this.tokenStorage = new TokenStorage(context.getApplicationContext());
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request originalRequest = chain.request();

        String token = tokenStorage.getAccessToken();

        if (token == null || token.trim().isEmpty()) {
            return chain.proceed(originalRequest);
        }

        Request requestWithToken = originalRequest.newBuilder()
                .header("Authorization", "Bearer " + token.trim())
                .build();

        return chain.proceed(requestWithToken);
    }
}
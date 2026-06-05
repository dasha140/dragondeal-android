package com.elvsf.dragondeal.api;

import com.elvsf.dragondeal.dto.AuthResponse;
import com.elvsf.dragondeal.dto.LoginRequest;
import com.elvsf.dragondeal.dto.PasswordResetConfirmRequest;
import com.elvsf.dragondeal.dto.PasswordResetRequest;
import com.elvsf.dragondeal.dto.RegisterRequest;
import com.elvsf.dragondeal.dto.FirebaseLoginRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import com.elvsf.dragondeal.dto.MessageResponse;
import com.elvsf.dragondeal.dto.RequestEmailChangeRequest;
import com.elvsf.dragondeal.dto.ConfirmEmailChangeRequest;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AuthApi {

    @POST("auth/firebase-login")
    Call<AuthResponse> firebaseLogin(@Body FirebaseLoginRequest request);

    @GET("auth/check-username")
    Call<MessageResponse> checkUsername(@Query("username") String username);

    @GET("auth/check-email")
    Call<MessageResponse> checkEmail(@Query("email") String email);

    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("auth/email-change/request")
    Call<MessageResponse> requestEmailChange(@Body RequestEmailChangeRequest request);

    @POST("auth/email-change/confirm")
    Call<AuthResponse> confirmEmailChange(@Body ConfirmEmailChangeRequest request);

    @POST("auth/password-reset/request")
    Call<MessageResponse> requestPasswordReset(@Body PasswordResetRequest request);

    @POST("auth/password-reset/confirm")
    Call<AuthResponse> confirmPasswordReset(@Body PasswordResetConfirmRequest request);
}
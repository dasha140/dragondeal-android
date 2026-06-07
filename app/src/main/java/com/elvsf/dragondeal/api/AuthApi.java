package com.elvsf.dragondeal.api;

import com.elvsf.dragondeal.dto.AuthResponse;
import com.elvsf.dragondeal.dto.ConfirmEmailChangeRequest;
import com.elvsf.dragondeal.dto.LoginRequest;
import com.elvsf.dragondeal.dto.MessageResponse;
import com.elvsf.dragondeal.dto.PasswordResetConfirmRequest;
import com.elvsf.dragondeal.dto.PasswordResetRequest;
import com.elvsf.dragondeal.dto.RegisterConfirmRequest;
import com.elvsf.dragondeal.dto.RegisterRequest;
import com.elvsf.dragondeal.dto.RequestEmailChangeRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface AuthApi {

    @GET("auth/check-username")
    Call<MessageResponse> checkUsername(@Query("username") String username);

    @GET("auth/check-email")
    Call<MessageResponse> checkEmail(@Query("email") String email);

    @POST("auth/register/request")
    Call<MessageResponse> registerRequest(@Body RegisterRequest request);

    @POST("auth/register/confirm")
    Call<AuthResponse> registerConfirm(@Body RegisterConfirmRequest request);

    @POST("auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("auth/email-change/request")
    Call<MessageResponse> requestEmailChange(@Body RequestEmailChangeRequest request);

    @POST("auth/email-change/confirm")
    Call<AuthResponse> confirmEmailChange(@Body ConfirmEmailChangeRequest request);

    @POST("auth/password-reset/request")
    Call<MessageResponse> requestPasswordReset(@Body PasswordResetRequest request);

    @POST("auth/password-reset/confirm")
    Call<AuthResponse> confirmPasswordReset(@Body PasswordResetConfirmRequest request);
}

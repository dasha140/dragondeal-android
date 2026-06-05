package com.elvsf.dragondeal.api;

import com.elvsf.dragondeal.dto.UpdateUserProfileRequest;
import com.elvsf.dragondeal.dto.UserResponse;
import com.elvsf.dragondeal.dto.AuthResponse;
import com.elvsf.dragondeal.dto.UpdatePersonalDataRequest;

import retrofit2.http.PATCH;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;

public interface UserApi {

    @GET("users/{userId}")
    Call<UserResponse> findUserById(@Path("userId") Long userId);

    @PATCH("users/me/profile")
    Call<UserResponse> updateMyProfile(@Body UpdateUserProfileRequest request);

    @PATCH("users/me/personal-data")
    Call<AuthResponse> updatePersonalData(@Body UpdatePersonalDataRequest request);
}
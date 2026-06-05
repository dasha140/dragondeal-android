package com.elvsf.dragondeal.api;

import com.elvsf.dragondeal.dto.UploadImageResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface UploadApi {

    @Multipart
    @POST("uploads/listings/images")
    Call<UploadImageResponse> uploadListingImage(
            @Part MultipartBody.Part file
    );

    @Multipart
    @POST("uploads/avatars")
    Call<UploadImageResponse> uploadAvatar(@Part MultipartBody.Part file);
}
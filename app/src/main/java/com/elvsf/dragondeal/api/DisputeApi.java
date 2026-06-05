package com.elvsf.dragondeal.api;

import com.elvsf.dragondeal.dto.CreateDisputeRequest;
import com.elvsf.dragondeal.dto.DisputeResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface DisputeApi {

    @POST("disputes")
    Call<DisputeResponse> createDispute(@Body CreateDisputeRequest request);

    @GET("disputes/order/{orderId}")
    Call<List<DisputeResponse>> findDisputesByOrder(@Path("orderId") Long orderId);
}
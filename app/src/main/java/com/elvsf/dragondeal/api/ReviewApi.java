package com.elvsf.dragondeal.api;

import com.elvsf.dragondeal.dto.CreateReviewRequest;
import com.elvsf.dragondeal.dto.ReviewResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ReviewApi {

    @POST("reviews")
    Call<ReviewResponse> createReview(@Body CreateReviewRequest request);

    @GET("reviews/seller/{sellerId}")
    Call<List<ReviewResponse>> getSellerReviews(@Path("sellerId") Long sellerId);
}

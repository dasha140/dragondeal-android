package com.elvsf.dragondeal.api;

import com.elvsf.dragondeal.dto.CreateListingRequest;
import com.elvsf.dragondeal.dto.ListingResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ListingApi {

    @GET("listings")
    Call<List<ListingResponse>> findAllListings();

    @GET("listings/active")
    Call<List<ListingResponse>> findActiveListings();

    @GET("listings/{id}")
    Call<ListingResponse> findListingById(@Path("id") Long id);

    @GET("listings/seller/me")
    Call<List<ListingResponse>> findMyListings();

    @GET("listings/seller/{sellerId}")
    Call<List<ListingResponse>> findListingsBySellerId(@Path("sellerId") Long sellerId);

    @POST("listings")
    Call<ListingResponse> createListing(@Body CreateListingRequest request);

    @PATCH("listings/{listingId}/deactivate")
    Call<Void> deactivateListing(@Path("listingId") Long listingId);
}
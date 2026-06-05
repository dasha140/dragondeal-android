package com.elvsf.dragondeal.api;

import com.elvsf.dragondeal.api.payment.OrderCompleteRequest;
import com.elvsf.dragondeal.dto.CreateOrderRequest;
import com.elvsf.dragondeal.dto.MarkOrderPaidRequest;
import com.elvsf.dragondeal.dto.OrderResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface OrderApi {

    @GET("orders")
    Call<List<OrderResponse>> findAllOrders();

    @GET("orders/{orderId}")
    Call<OrderResponse> findOrderById(@Path("orderId") Long orderId);

    @POST("orders")
    Call<OrderResponse> createOrder(@Body CreateOrderRequest request);

    @GET("orders/buyer/me")
    Call<List<OrderResponse>> findMyBuyerOrders();

    @GET("orders/seller/me")
    Call<List<OrderResponse>> findMySellerOrders();

    @GET("orders/seller/{sellerId}")
    Call<List<OrderResponse>> findSellerOrders(@Path("sellerId") Long sellerId);

    @GET("orders/status/{status}")
    Call<List<OrderResponse>> findOrdersByStatus(@Path("status") String status);

    @PATCH("orders/{orderId}/paid")
    Call<Void> markOrderAsPaid(
            @Path("orderId") Long orderId,
            @Body MarkOrderPaidRequest request
    );

    @PATCH("orders/{orderId}/seller-done")
    Call<Void> markSellerDone(@Path("orderId") Long orderId);

    @PATCH("orders/{orderId}/complete")
    Call<Void> completeOrder(
            @Path("orderId") Long orderId,
            @Body OrderCompleteRequest request
    );

    @PATCH("orders/{orderId}/cancel")
    Call<Void> cancelOrder(@Path("orderId") Long orderId);
}
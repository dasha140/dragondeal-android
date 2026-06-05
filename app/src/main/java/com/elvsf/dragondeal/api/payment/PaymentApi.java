package com.elvsf.dragondeal.api.payment;



import java.util.List;
import com.elvsf.dragondeal.dto.OrderResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface PaymentApi {

    @GET("payment-methods")
    Call<List<PaymentMethodResponse>> getEnabledPaymentMethods();

    @GET("payment-methods/all")
    Call<List<PaymentMethodResponse>> getAllPaymentMethods();

    @POST("payment-intents")
    Call<PaymentIntentResponse> createPaymentIntent(
            @Body PaymentIntentRequest request
    );

    @GET("payment-intents/{paymentIntentId}")
    Call<PaymentIntentResponse> getPaymentIntent(
            @Path("paymentIntentId") Long paymentIntentId
    );

    @PATCH("payment-intents/{paymentIntentId}/tx")
    Call<PaymentIntentResponse> submitPaymentTx(
            @Path("paymentIntentId") Long paymentIntentId,
            @Body PaymentTxSubmitRequest request
    );

    @PATCH("payment-intents/{paymentIntentId}/verify")
    Call<PaymentIntentResponse> verifyPayment(
            @Path("paymentIntentId") Long paymentIntentId
    );

    @PATCH("orders/{orderId}/seller-done")
    Call<Void> markSellerDone(
            @Path("orderId") Long orderId
    );

    @PATCH("orders/{orderId}/complete")
    Call<Void> completeOrder(
            @Path("orderId") Long orderId,
            @Body OrderCompleteRequest request
    );
}
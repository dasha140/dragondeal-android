package com.elvsf.dragondeal.api.payment;

import android.content.Context;

import com.elvsf.dragondeal.api.RetrofitClient;
import com.elvsf.dragondeal.dto.OrderResponse;

import java.util.List;

import retrofit2.Call;

public class PaymentRepository {

    private final PaymentApi paymentApi;

    public PaymentRepository(Context context) {
        paymentApi = RetrofitClient.getInstance(context).create(PaymentApi.class);
    }

    public Call<List<PaymentMethodResponse>> getEnabledPaymentMethods() {
        return paymentApi.getEnabledPaymentMethods();
    }

    public Call<List<PaymentMethodResponse>> getAllPaymentMethods() {
        return paymentApi.getAllPaymentMethods();
    }

    public Call<PaymentIntentResponse> createPaymentIntent(
            Long orderId,
            Long paymentMethodId
    ) {
        PaymentIntentRequest request = new PaymentIntentRequest(orderId, paymentMethodId);
        return paymentApi.createPaymentIntent(request);
    }

    public Call<PaymentIntentResponse> getPaymentIntent(Long paymentIntentId) {
        return paymentApi.getPaymentIntent(paymentIntentId);
    }

    public Call<PaymentIntentResponse> submitPaymentTx(
            Long paymentIntentId,
            String escrowDealId,
            String paymentTxHash
    ) {
        PaymentTxSubmitRequest request = new PaymentTxSubmitRequest(
                escrowDealId,
                paymentTxHash
        );

        return paymentApi.submitPaymentTx(paymentIntentId, request);
    }

    public Call<PaymentIntentResponse> verifyPayment(Long paymentIntentId) {
        return paymentApi.verifyPayment(paymentIntentId);
    }

    public Call<Void> markSellerDone(Long orderId) {
        return paymentApi.markSellerDone(orderId);
    }

    public Call<Void> completeOrder(
            Long orderId,
            String releaseTxHash
    ) {
        OrderCompleteRequest request = new OrderCompleteRequest(releaseTxHash);
        return paymentApi.completeOrder(orderId, request);
    }
}
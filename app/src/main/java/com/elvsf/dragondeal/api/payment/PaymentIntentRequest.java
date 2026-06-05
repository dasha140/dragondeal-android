package com.elvsf.dragondeal.api.payment;

public class PaymentIntentRequest {

    private Long orderId;
    private Long paymentMethodId;

    public PaymentIntentRequest(Long orderId, Long paymentMethodId) {
        this.orderId = orderId;
        this.paymentMethodId = paymentMethodId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getPaymentMethodId() {
        return paymentMethodId;
    }
}
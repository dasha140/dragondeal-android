package com.elvsf.dragondeal.api.payment;

public class PaymentTxSubmitRequest {

    private String escrowDealId;
    private String paymentTxHash;

    public PaymentTxSubmitRequest(String escrowDealId, String paymentTxHash) {
        this.escrowDealId = escrowDealId;
        this.paymentTxHash = paymentTxHash;
    }

    public String getEscrowDealId() {
        return escrowDealId;
    }

    public String getPaymentTxHash() {
        return paymentTxHash;
    }
}
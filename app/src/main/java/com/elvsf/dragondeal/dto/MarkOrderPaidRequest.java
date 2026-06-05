package com.elvsf.dragondeal.dto;

public class MarkOrderPaidRequest {

    private String paymentTxHash;

    public MarkOrderPaidRequest(String paymentTxHash) {
        this.paymentTxHash = paymentTxHash;
    }

    public String getPaymentTxHash() {
        return paymentTxHash;
    }
}
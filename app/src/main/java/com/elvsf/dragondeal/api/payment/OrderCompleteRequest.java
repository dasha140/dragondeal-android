package com.elvsf.dragondeal.api.payment;

public class OrderCompleteRequest {

    private String releaseTxHash;

    public OrderCompleteRequest(String releaseTxHash) {
        this.releaseTxHash = releaseTxHash;
    }

    public String getReleaseTxHash() {
        return releaseTxHash;
    }
}
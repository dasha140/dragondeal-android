package com.elvsf.dragondeal.dto;

public class CreateDisputeRequest {

    private Long orderId;
    private String reason;

    public CreateDisputeRequest(Long orderId, String reason) {
        this.orderId = orderId;
        this.reason = reason;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getReason() {
        return reason;
    }
}
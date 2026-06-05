package com.elvsf.dragondeal.dto;

public class DisputeResponse {

    private Long id;
    private Long orderId;
    private Long openedById;
    private String openedByUsername;
    private String reason;
    private String status;
    private String adminDecision;
    private String createdAt;

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getOpenedById() {
        return openedById;
    }

    public String getOpenedByUsername() {
        return openedByUsername;
    }

    public String getReason() {
        return reason;
    }

    public String getStatus() {
        return status;
    }

    public String getAdminDecision() {
        return adminDecision;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
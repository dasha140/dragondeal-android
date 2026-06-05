package com.elvsf.dragondeal.dto;

import java.math.BigDecimal;

public class OrderResponse {

    private Long id;
    private Long buyerId;
    private String buyerUsername;
    private Long sellerId;
    private String sellerUsername;
    private Long listingId;
    private String listingTitle;
    private BigDecimal totalPrice;
    private String currency;
    private String network;
    private String status;
    private String escrowContract;
    private String escrowDealId;
    private String paymentTxHash;
    private String releaseTxHash;
    private String createdAt;
    private String paidAt;
    private String sellerMarkedDoneAt;
    private String completedAt;

    public Long getId() {
        return id;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public String getBuyerUsername() {
        return buyerUsername;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public String getSellerUsername() {
        return sellerUsername;
    }

    public Long getListingId() {
        return listingId;
    }

    public String getListingTitle() {
        return listingTitle;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public String getNetwork() {
        return network;
    }

    public String getStatus() {
        return status;
    }

    public String getEscrowContract() {
        return escrowContract;
    }

    public String getEscrowDealId() {
        return escrowDealId;
    }

    public String getPaymentTxHash() {
        return paymentTxHash;
    }

    public String getReleaseTxHash() {
        return releaseTxHash;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getPaidAt() {
        return paidAt;
    }

    public String getSellerMarkedDoneAt() {
        return sellerMarkedDoneAt;
    }

    public String getCompletedAt() {
        return completedAt;
    }
}
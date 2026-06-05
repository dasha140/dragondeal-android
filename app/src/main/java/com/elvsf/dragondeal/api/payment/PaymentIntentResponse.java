package com.elvsf.dragondeal.api.payment;

import java.math.BigDecimal;

public class PaymentIntentResponse {

    private Long id;
    private Long orderId;
    private Long buyerId;
    private Long sellerId;
    private Long paymentMethodId;

    private String currency;
    private String network;

    private BigDecimal amount;
    private String amountAtomic;

    private Long chainId;
    private String tokenContract;
    private String escrowContract;

    private String escrowDealId;
    private String paymentTxHash;

    private String status;

    private String createdAt;
    private String expiresAt;
    private String paidAt;

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public Long getPaymentMethodId() {
        return paymentMethodId;
    }

    public String getCurrency() {
        return currency;
    }

    public String getNetwork() {
        return network;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getAmountAtomic() {
        return amountAtomic;
    }

    public Long getChainId() {
        return chainId;
    }

    public String getTokenContract() {
        return tokenContract;
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

    public String getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public String getPaidAt() {
        return paidAt;
    }
}
package com.elvsf.dragondeal.api.payment;

import java.math.BigDecimal;

public class PaymentMethodResponse {

    private Long id;
    private String currency;
    private String network;
    private Long chainId;
    private String tokenContract;
    private String escrowContract;
    private Integer decimals;
    private BigDecimal minAmount;
    private Boolean enabled;

    public Long getId() {
        return id;
    }

    public String getCurrency() {
        return currency;
    }

    public String getNetwork() {
        return network;
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

    public Integer getDecimals() {
        return decimals;
    }

    public BigDecimal getMinAmount() {
        return minAmount;
    }

    public Boolean getEnabled() {
        return enabled;
    }
}
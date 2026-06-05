package com.elvsf.dragondeal.dto;

public class CreateOrderRequest {

    private Long listingId;

    public CreateOrderRequest(Long listingId) {
        this.listingId = listingId;
    }

    public Long getListingId() {
        return listingId;
    }
}
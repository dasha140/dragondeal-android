package com.elvsf.dragondeal.dto;

import java.math.BigDecimal;
import java.util.List;

public class CreateListingRequest {

    private Long categoryId;
    private Long gameId;
    private String title;
    private String description;
    private BigDecimal price;
    private String currency;
    private String network;
    private String deliveryType;
    private List<String> imageUrls;

    public CreateListingRequest(
            Long categoryId,
            Long gameId,
            String title,
            String description,
            BigDecimal price,
            String currency,
            String network,
            String deliveryType,
            List<String> imageUrls
    ) {
        this.categoryId = categoryId;
        this.gameId = gameId;
        this.title = title;
        this.description = description;
        this.price = price;
        this.currency = currency;
        this.network = network;
        this.deliveryType = deliveryType;
        this.imageUrls = imageUrls;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public Long getGameId() {
        return gameId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }

    public String getNetwork() {
        return network;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }
}
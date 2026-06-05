package com.elvsf.dragondeal.dto;

import java.math.BigDecimal;
import java.util.List;

public class ListingResponse {

    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private String currency;
    private String network;
    private String deliveryType;
    private String status;
    private String createdAt;
    private String updatedAt;

    private Long sellerId;
    private String sellerUsername;
    private String sellerAvatarUrl;
    private BigDecimal sellerRating;
    private Long sellerCompletedOrdersCount;

    private Long categoryId;
    private String categoryName;

    private Long gameId;
    private String gameName;
    private String gameImageUrl;

    private List<String> imageUrls;

    public Long getId() {
        return id;
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

    public String getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public String getSellerUsername() {
        return sellerUsername;
    }

    public String getSellerAvatarUrl() {
        return sellerAvatarUrl;
    }

    public BigDecimal getSellerRating() {
        return sellerRating;
    }

    public Long getSellerCompletedOrdersCount() {
        return sellerCompletedOrdersCount;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public Long getGameId() {
        return gameId;
    }

    public String getGameName() {
        return gameName;
    }

    public String getGameImageUrl() {
        return gameImageUrl;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }
}
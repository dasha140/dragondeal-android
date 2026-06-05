package com.elvsf.dragondeal.dto;

public class CreateReviewRequest {

    private Long orderId;
    private Integer rating;
    private String comment;

    public CreateReviewRequest(Long orderId, Integer rating, String comment) {
        this.orderId = orderId;
        this.rating = rating;
        this.comment = comment;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Integer getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }
}
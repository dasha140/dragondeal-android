package com.elvsf.dragondeal.dto;

public class ReviewResponse {

    private Long id;
    private Long orderId;
    private Long reviewerId;
    private String reviewerUsername;
    private Long targetUserId;
    private String targetUsername;
    private Integer rating;
    private String comment;
    private String createdAt;

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public String getReviewerUsername() {
        return reviewerUsername;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public String getTargetUsername() {
        return targetUsername;
    }

    public Integer getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
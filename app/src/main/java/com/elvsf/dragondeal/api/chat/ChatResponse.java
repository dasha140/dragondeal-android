package com.elvsf.dragondeal.api.chat;

import java.util.List;

public class ChatResponse {

    private Long id;
    private Long orderId;
    private Long buyerId;
    private String buyerUsername;
    private Long sellerId;
    private String sellerUsername;
    private String createdAt;
    private List<ChatMessageResponse> messageList;

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
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

    public String getCreatedAt() {
        return createdAt;
    }

    public List<ChatMessageResponse> getMessageList() {
        return messageList;
    }
}
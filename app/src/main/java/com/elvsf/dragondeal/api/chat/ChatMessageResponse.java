package com.elvsf.dragondeal.api.chat;

public class ChatMessageResponse {

    private Long id;
    private Long chatId;
    private Long senderId;
    private String senderUsername;
    private String content;
    private String imageUrl;
    private String createdAt;

    public Long getId() {
        return id;
    }

    public Long getChatId() {
        return chatId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public String getContent() {
        return content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
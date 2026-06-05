package com.elvsf.dragondeal.api.chat;

public class SendChatMessageRequest {

    private Long chatId;
    private String content;
    private String imageUrl;

    public SendChatMessageRequest(Long chatId, String content, String imageUrl) {
        this.chatId = chatId;
        this.content = content;
        this.imageUrl = imageUrl;
    }

    public Long getChatId() {
        return chatId;
    }

    public String getContent() {
        return content;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
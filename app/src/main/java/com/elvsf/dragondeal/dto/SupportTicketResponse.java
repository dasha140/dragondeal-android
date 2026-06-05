package com.elvsf.dragondeal.dto;

public class SupportTicketResponse {

    private Long id;
    private String subject;
    private String message;
    private String adminReply;
    private String status;
    private String createdAt;
    private String updatedAt;
    private String repliedAt;

    public Long getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public String getMessage() {
        return message;
    }

    public String getAdminReply() {
        return adminReply;
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

    public String getRepliedAt() {
        return repliedAt;
    }
}

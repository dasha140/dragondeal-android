package com.elvsf.dragondeal.dto;

public class SupportTicketRequest {

    private String subject;
    private String message;

    public SupportTicketRequest(String subject, String message) {
        this.subject = subject;
        this.message = message;
    }

    public String getSubject() {
        return subject;
    }

    public String getMessage() {
        return message;
    }
}

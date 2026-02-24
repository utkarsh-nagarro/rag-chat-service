package com.ragchat.api.dto;

import jakarta.validation.constraints.NotBlank;

public class AddMessageRequest {

    @NotBlank
    private String sender; // USER or ASSISTANT

    @NotBlank
    private String content;

    @NotBlank
    private String userId;

    private String context;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}


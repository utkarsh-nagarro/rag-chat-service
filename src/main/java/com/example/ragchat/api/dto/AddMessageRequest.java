package com.example.ragchat.api.dto;

import jakarta.validation.constraints.NotBlank;

public class AddMessageRequest {

    @NotBlank
    private String sender; // USER or ASSISTANT

    @NotBlank
    private String content;

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
}


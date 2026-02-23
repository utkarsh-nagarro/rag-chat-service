package com.example.ragchat.api.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateSessionRequest {

    @NotBlank
    private String userId;

    @NotBlank
    private String title;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}


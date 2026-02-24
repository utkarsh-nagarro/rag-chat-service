package com.ragchat.api.dto;

import jakarta.validation.constraints.NotBlank;

public class RenameSessionRequest {

    @NotBlank
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}


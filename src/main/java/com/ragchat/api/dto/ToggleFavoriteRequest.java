package com.ragchat.api.dto;

public class ToggleFavoriteRequest {

    private boolean favorite;

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}


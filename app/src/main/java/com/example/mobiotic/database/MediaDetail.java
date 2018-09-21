package com.example.mobiotic.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class MediaDetail {

    @NonNull
    @PrimaryKey
    private String id;
    private String description;
    private String thumb;
    private String title;
    private String url;
    private Long lastPlayedPostion;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Long getLastPlayedPostion() {
        return lastPlayedPostion;
    }

    public void setLastPlayedPostion(Long lastPlayedPostion) {
        this.lastPlayedPostion = lastPlayedPostion;
    }
}

package com.portfolio.school.model;

import android.graphics.Bitmap;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "chat_history")
public class DataResponse {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private final int isUser;
    private final String prompt;
    private String imageUri = "";
    private final long timestamp;

    @Ignore
    private boolean isAnimate = true;

    @Ignore
    private Bitmap bitmap;


    public DataResponse(int isUser, String prompt, String imageUri, long timestamp) {
        this.isUser = isUser;
        this.prompt = prompt;
        this.imageUri = imageUri;
        this.timestamp = timestamp;
    }
    @Ignore
    public DataResponse(int isUser, String prompt, Bitmap bitmap, String imageUri, long timestamp) {
        this.isUser = isUser;
        this.prompt = prompt;
        this.bitmap = bitmap;
        this.imageUri = imageUri;
        this.timestamp = timestamp;
    }



    public void setId(int id) { this.id = id; }

    public int getId() { return id; }

    public int isUser() { // 또는 public int getIsUser()
        return isUser;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getImageUri() {
        return imageUri;
    }

    public long getTimestamp() { return timestamp; }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataResponse that = (DataResponse) o;
        if (id != that.id) return false;
        if (isUser != that.isUser) return false;
        if (!prompt.equals(that.prompt)) return false;
        if (!imageUri.equals(that.imageUri)) return false;
        return imageUri.equals(that.imageUri);
    }

    @Override
    public int hashCode() {
        int result = isUser;
        result = 31 * result + id;
        result = 31 * result + prompt.hashCode();
        result = 31 * result + imageUri.hashCode();
        result = 31 * result + Long.hashCode(timestamp);
        return result;
    }

    @Override
    public String toString() {
        return "DataResponse{" +
                "id=" + id +
                "isUser=" + isUser +
                ", prompt='" + prompt + '\'' +
                ", imageUri='" + imageUri + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }


    public boolean isAnimate() {
        return isAnimate;
    }

    public void setAnimate(boolean animate) {
        isAnimate = animate;
    }
}

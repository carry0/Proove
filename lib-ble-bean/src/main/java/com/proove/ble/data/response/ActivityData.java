package com.proove.ble.data.response;

public class ActivityData {
    private String id;
    private String title;
    private String image;
    private String content;
    private String date;
    private String url;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "ActivityData{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", image='" + image + '\'' +
                ", content='" + content + '\'' +
                ", date='" + date + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
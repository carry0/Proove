package com.proove.ble.data.response;

import java.util.List;

public class FeedbackData {

    private String id;
    private String content;
    private String contact;
    private String uid;
    private String type;
    private String pid;
    private List<String> imageList;
    private String addDate;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getContact() {
        return contact;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getPid() {
        return pid;
    }

    public void setImageList(List<String> imageList) {
        this.imageList = imageList;
    }

    public List<String> getImageList() {
        return imageList;
    }

    public void setAddDate(String addDate) {
        this.addDate = addDate;
    }

    public String getAddDate() {
        return addDate;
    }

}
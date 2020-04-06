package com.hooplachat.Model;

public class StatusPost {
    private String currentDate;
    private String currentTime;
    private String message;
    private String postID;
    private String userID;

    public StatusPost(String currentDate, String currentTime, String message, String postID, String userID) {
        this.currentDate = currentDate;
        this.currentTime = currentTime;
        this.message = message;
        this.postID = postID;
        this.userID = userID;
    }

    public StatusPost() {
    }

    public String getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(String currentDate) {
        this.currentDate = currentDate;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}

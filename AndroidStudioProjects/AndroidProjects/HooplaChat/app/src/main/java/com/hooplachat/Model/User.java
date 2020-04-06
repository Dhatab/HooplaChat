package com.hooplachat.Model;

public class User {

    private String id;
    private String username;
    private String displayName;
    private String imageURL;
    private String status;
    private String search;

    public User() {
    }

    public User(String id, String username, String displayName, String imageURL, String status, String search) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.imageURL = imageURL;
        this.status = status;
        this.search = search;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }
}
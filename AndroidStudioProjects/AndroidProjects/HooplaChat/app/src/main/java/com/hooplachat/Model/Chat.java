package com.hooplachat.Model;

public class Chat {

    private String sender;
    private String receiver;
    private String message;
    private boolean read_text;

    public Chat(String sender, String receiver, String message, boolean read_text) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.read_text = read_text;
    }

    public Chat() {
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead_text() {
        return read_text;
    }

    public void setRead_text(boolean read_text) {
        this.read_text = read_text;
    }
}

package com.example.kuetride.model;

public class Message {
    private String id;
    private String userId;
    private String message;
    private long timestamp;

    public Message(String id, String userId, String message, long timestamp) {
        this.id = id;
        this.userId = userId;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }
}

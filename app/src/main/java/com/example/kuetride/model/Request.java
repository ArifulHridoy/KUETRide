package com.example.kuetride.model;

public class Request {
    private String id;
    private String studentId;
    private String type;
    private String description;
    private String status;

    public Request(String id, String studentId, String type, String description, String status) {
        this.id = id;
        this.studentId = studentId;
        this.type = type;
        this.description = description;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }
}

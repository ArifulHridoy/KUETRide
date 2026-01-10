package com.example.kuetride.model;

public class User {
    private String id;
    private String username;
    private String email;
    private String department;
    private String assignedRoute;
    private String assignedBus;

    public User(String id, String username, String email, String department, String assignedRoute, String assignedBus) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.department = department;
        this.assignedRoute = assignedRoute;
        this.assignedBus = assignedBus;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getDepartment() {
        return department;
    }

    public String getAssignedRoute() {
        return assignedRoute;
    }

    public String getAssignedBus() {
        return assignedBus;
    }
}

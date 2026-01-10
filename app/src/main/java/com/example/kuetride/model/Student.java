package com.example.kuetride.model;

public class Student {
    private String userId;
    private String name;
    private String email;
    private String department;
    private String rollNumber;
    private String phone;
    private String assignedRoute;
    private String assignedBus;

    public Student() {}

    public Student(String userId, String name, String email, String department, String rollNumber, String phone, String assignedRoute, String assignedBus) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.department = department;
        this.rollNumber = rollNumber;
        this.phone = phone;
        this.assignedRoute = assignedRoute;
        this.assignedBus = assignedBus;
    }

    // Getters and setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAssignedRoute() { return assignedRoute; }
    public void setAssignedRoute(String assignedRoute) { this.assignedRoute = assignedRoute; }
    public String getAssignedBus() { return assignedBus; }
    public void setAssignedBus(String assignedBus) { this.assignedBus = assignedBus; }
    public String getUsername() { return name; }
    public String getId() { return userId; }
}

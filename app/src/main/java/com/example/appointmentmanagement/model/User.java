package com.example.appointmentmanagement.model;

public class User {
    private String id;
    private String email;
    private String role;
    private String phone;
    private String avatar;
    private String workingHours;

    public User() {
        // Constructor mặc định cần thiết cho Firestore
    }

    public String getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(String workingHours) {
        this.workingHours = workingHours;
    }

    public User(String id, String email, String role, String workingHours, String avatar) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.workingHours = workingHours;
    }

    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getAvatar() { return avatar; }
    public String getPhone() { return phone; }
}

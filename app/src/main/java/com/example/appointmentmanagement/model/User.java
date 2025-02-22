package com.example.appointmentmanagement.model;

public class User {
    private String id;
    private String email;
    private String role;
    private String phone;
    private String avatar;

    public User() {
        // Constructor mặc định cần thiết cho Firestore
    }

    public User(String id, String email, String role, String phone, String avatar) {
        this.id = id;
        this.email = email;
        this.role = role;
    }

    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getAvatar() { return avatar; }
    public String getPhone() { return phone; }
}

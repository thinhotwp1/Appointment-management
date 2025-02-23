package com.example.appointmentmanagement.model;

public class Service {
    private String id;
    private String name;

    // Constructor mặc định (cần thiết cho Firestore)
    public Service() {}

    // Constructor đầy đủ
    public Service(String id, String name) {
        this.id = id;
        this.name = name;
    }

    // Constructor chỉ có name (dùng khi tạo mới)
    public Service(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
